/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package migration.auth.digits.google.com.digitsmigrationhelpers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.digits.auth.migration.internal.ClearSessionContinuation;
import com.google.digits.auth.migration.internal.StorageHelpers;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.util.WeakHashMap;

import static android.support.annotation.VisibleForTesting.PRIVATE;

/**
 * Helper to exchange a tokens issued by the Digits SDK for a user in the new
 * Firebase SDK. See examples on github for usage.
 */
public final class AuthMigrator {
    @NonNull
    private static final WeakHashMap<FirebaseApp, AuthMigrator> instances = new WeakHashMap<>();
    @NonNull
    private final StorageHelpers mStorageHelpers;
    @NonNull
    private final FirebaseAuth mFirebaseAuth;
    @NonNull
    private final FirebaseApp mApp;
    @NonNull
    private final ClearSessionContinuation mClearSessionContinuation;
    @NonNull
    private static final Task<Void> VOID_TASK = Tasks.forResult(null);
    @NonNull
    private static final Continuation<AuthResult, Task<Void>> VOID_CONTINUATION =
            new Continuation<AuthResult, Task<Void>> () {
                @Override
                public Task<Void> then(@NonNull Task<AuthResult> task) throws Exception {
                    if(task.isSuccessful()) {
                        return VOID_TASK;
                    }
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        return Tasks.forException(e);
                    }
                }
    };

    private static final String TAG = "DigitsAuthMigrator";

    /**
     * Gets an instance of {@link AuthMigrator}
     *
     * @param app Firebase mApp instance used to construct authMigrator.
     *            Use {@link #getInstance(FirebaseApp)} to create using the default mApp.
     * @return AuthMigrator for the firebase mApp
     */
    public static AuthMigrator getInstance(FirebaseApp app) {
        synchronized (instances) {
            AuthMigrator instance = instances.get(app);
            if (instance == null) {
                instance = new AuthMigrator(app, new StorageHelpers(app.getApplicationContext()),
                        FirebaseAuth.getInstance());
                instances.put(app, instance);
            }
            return instance;
        }
    }

    /**
     * Gets an instance of the {@link AuthMigrator} with the mApp returned by
     * {@link FirebaseApp#getInstance()}
     *
     * @return AuthMigrator for default mApp
     */
    public static AuthMigrator getInstance() {
        return getInstance(FirebaseApp.getInstance());
    }


    /**
     * gets the {@link FirebaseApp} for this {@link AuthMigrator} instance
     *
     * @return mApp
     */
    public FirebaseApp getApp() {
        return mApp;
    }

    /**
     * Migrates a user token from the Digits SDK, making that user the current user
     * in the new Firebase SDK. This is the recommended method that most apps would want to use.
     * This method works as follows:
     * <ol> <li>If a user is already logged in with the new Firebase SDK, then the legacy auth token
     * will be (optionally) removed, but the logged in user will not be affected</li>
     * <li>Looks up the legacy digits auth token.</li>
     * <li>Looks for the following keys in the mApp' s AndroidManifest.xml:
     * <ol><li type="circle">"io.fabric.ApiKey"</li>
     *     <li type="circle">"com.digits.sdk.android.ConsumerKey"</li>
     *     <li type="circle">"com.digits.sdk.android.ConsumerSecret"</li>
     * </ol></li>
     * <li>Sends the legacy token and  keys to a Firebase server to exchange it for a new
     * Firebase auth token.</li>
     * <li>Uses the new auth token to log in the user.</li>
     * <li>If the Firebase server determines that the legacy auth token is invalid, it will be
     * (optionally) removed from the device and the user will not be logged in.</li></ol>
     * Sample:
     * <pre>
     * <code>AuthMigrator.getInstance().migrate().addOnCompleteListener(new{@code
     * OnCompleteListener<AuthResult>}() {
     *    {@literal @}Override
     *     public void onComplete({@code Task<AuthResult>}task) {
     *          if (task.isSuccessful()) {
     *              FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
     *              if (u != null) {
     *                  // Either a user was already logged in or token exchange succeeded
     *                  Log.d("Digits", "Preserved Phone Number" + u.getPhoneNumber());
     *                  Log.d("Digits", "Preserved User Id" + u.getUid());
     *              } else {
     *                  // No valid digits session was found
     *                  Log.d("Digits", "No valid legacy digits session found");
     *              }
     *          } else {
     *              //an error accured
     *              Log.d("Digits", "Error upgrading digits session: " + task.getException()
     *                  .getLocalizedMessage());
     *          }
     *     }
     * });</code></pre>
     * @param cleanupDigitsSession whether the legacy digits session should be cleaned up after a
     *                             successful exchange or if found to be invalid.
     * @return task representing the token exchange process. Apps can listen to the status of the
     * returned task using {@link Task#addOnCompleteListener(Activity, OnCompleteListener)}.
     * The task succeeds in all of the following situations:
     * <ol><li>Legacy digits session was successfuly exchanged</li>
     * <li>No legacy digits session was found</li>
     * <li>An existing firebase session was found and no attempt was made to exchange the digits
     * token</li>
     * </ol>
     * The task fails only when digits token was found and:
     * <ol><li>The server was unable to validate it. The corrupt session is automatically
     * (optionally) cleared</li>
     * <li>The server failed for internal reasons. The legacy session is retained to permit
     * retries initiated from the mApp</li>
     * </ol>
     */
    public Task<Void> migrate(boolean cleanupDigitsSession) {
        final FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        final String sessionJson = mStorageHelpers.getDigitsSessionJson();
        final Context context = mApp.getApplicationContext();
        final RedeemableDigitsSessionBuilder builder;

        // If there's already a current user, don't migrate and clear the legacy token.
        if (currentUser != null) {
            Log.d(TAG, "Found existing firebase session. Skipping Exchange.");
            return cleanupAndCreateEmptyResult(cleanupDigitsSession);
        }

        // If no legacy session found, return
        if(sessionJson == null) {
            Log.d(TAG, "No digits session found");
            return cleanupAndCreateEmptyResult(cleanupDigitsSession);
        }

        Log.d(TAG, "Exchanging digits session");

        // If session is invalid, return
        try {
            builder = RedeemableDigitsSessionBuilder.fromSessionJson(sessionJson);
        } catch (JSONException e) {
            Log.d(TAG, "Digits sesion is corrupt");
            //invalid session
            return cleanupAndCreateEmptyResult(cleanupDigitsSession);
        }

        builder.setConsumerKey(mStorageHelpers.getApiKeyFromManifest(context,
                        StorageHelpers.DIGITS_CONSUMER_KEY_KEY))
                .setConsumerSecret(mStorageHelpers.getApiKeyFromManifest(context,
                        StorageHelpers.DIGITS_CONSUMER_SECRET_KEY))
                .setFabricApiKey(mStorageHelpers.getApiKeyFromManifest(context,
                        StorageHelpers.FABRIC_API_KEY_KEY));

        Task<Void> exchangeTask = mFirebaseAuth.signInWithCustomToken(
                mStorageHelpers.getUnsignedJWT(builder.build().getPayload()))
                .continueWithTask(VOID_CONTINUATION);

        return cleanupDigitsSession
                ? exchangeTask.continueWithTask(mClearSessionContinuation)
                : exchangeTask;
    }

    /**
     * Migrates a user token from the Digits SDK, making that user the current user
     * in the new Firebase SDK. This method can be used by apps that have tokens they wish to
     * exchange. To exchange the default tokens found on the client, use {@link #migrate(boolean)}
     * <p>
     * This works as follows:
     * <ol><li>Sends the legacy token and keys provided by the caller in the
     * {@link RedeemableDigitsSessionBuilder} to a Firebase server to exchange it for a new
     * Firebase auth token.</li>
     * <li>Uses the new auth token to log in the user.</li>
     * </ol>
     * <p>
     * If a user is already logged in with the new Firebase SDK, the logged in user will not be
     * affected.
     * <p>
     *
     * @param builder containing redeemable exchange info. Non optional parameters in the builder:
     *                (consumerKey, consumerSecret, fabricApiKey, authToken, authTokenSecret)
     * @return task representing the token exchange process. Apps can listen to the status of the
     * returned task using {@link Task#addOnCompleteListener(Activity, OnCompleteListener)}.
     * The task succeeds in all of the following situations:
     * <ol><li>Session was successfuly exchanged</li>
     * </ol>
     * The task fails when:
     * <ol><li>The server was unable to validate the token provided</li>
     * <li>The server failed for internal reasons</li>
     * </ol>
     */
    public Task<Void> migrate(@NonNull RedeemableDigitsSessionBuilder builder) {
        return mFirebaseAuth.signInWithCustomToken(
                mStorageHelpers.getUnsignedJWT(builder.build().getPayload()))
                .continueWithTask(VOID_CONTINUATION);
    }

    private Task<Void> cleanupAndCreateEmptyResult(boolean cleanupDigitsSession) {
        if(cleanupDigitsSession) {
            Log.d(TAG, "Clearing legacy session");
            mStorageHelpers.clearDigitsSession();
        }
        return VOID_TASK;
    }

    /**
     * Checks whether an auth token from the legacy SDK exists.  Uses the FirebaseApp's name
     * (or 'default' for the default mApp) as the persistence key.
     *
     * @return does the client contain a legacy digits token
     */
    public boolean hasLegacyAuth() {
        return mStorageHelpers.hasDigitsSession();
    }

    /**
     * Clears the auth token from the legacy SDK.  Uses the FirebaseApp's name (or 'default' for
     * the default mApp) as the persistence key.
     */
    public void clearLegacyAuth() {
        mStorageHelpers.clearDigitsSession();
    }

    @VisibleForTesting(otherwise = PRIVATE)
    AuthMigrator(@NonNull FirebaseApp app, @NonNull StorageHelpers storageHelper,
                 @NonNull FirebaseAuth firebaseAuth) {
        mApp = app;
        mStorageHelpers = storageHelper;
        mFirebaseAuth = firebaseAuth;
        mClearSessionContinuation = new ClearSessionContinuation(mStorageHelpers);
    }
}