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
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.digits.auth.migration.internal.ClearSessionContinuation;
import com.google.digits.auth.migration.internal.MigratorAuthResult;
import com.google.digits.auth.migration.internal.StorageHelpers;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.util.WeakHashMap;

/**
 * Helper to exchange a tokens issued by the Digits SDK for a user in the new
 * Firebase SDK. See examples on github for usage.
 */
public final class AuthMigrator {
    private static final WeakHashMap<FirebaseApp, AuthMigrator> instances = new WeakHashMap<>();
    private final StorageHelpers storageHelpers;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseApp app;

    /**
     * Gets an instance of {@link AuthMigrator}
     *
     * @param app Firebase app instance used to construct authMigrator. Use {@link #getInstance}
     *            to create using the default app.
     * @return AuthMigrator for the firebase app
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
     * Gets an instance of the {@link AuthMigrator} with the app returned by
     * {@link FirebaseApp#getInstance()}
     *
     * @return AuthMigrator for default app
     */
    public static AuthMigrator getInstance() {
        return getInstance(FirebaseApp.getInstance());
    }


    /**
     * gets the {@link FirebaseApp} for this {@link AuthMigrator} instance
     *
     * @return app
     */
    public FirebaseApp getApp() {
        return app;
    }

    /**
     * Migrates a user token from the Digits SDK, making that user the current user
     * in the new Firebase SDK. This method can be used by apps that have tokens they wish to
     * exchange. To exchange the tokens found on the client, use {@link #migrate()}
     * <p>
     * This works as follows:
     * <ol>
     * <li>Sends the legacy token and keys provided by the caller in the
     * {@link RedeemableDigitsSessionBuilder} to a Firebase server to exchange it for a new
     * Firebase auth token.</li>
     * <li>Uses the new auth token to log in the user.</li>
     * <li>Removes the legacy digits auth token from the device.</li>
     * </ol>
     * <p>
     * If a user is already logged in with the new Firebase SDK, then the legacy auth token will be
     * removed, but the logged in user will not be affected.
     * <p>
     * If the Firebase server determines that the legacy auth token is invalid, it will be removed
     * and the user will not be logged in.
     *
     * @param builder containing app info (consumer_key and secret), user info(OAuth token and
     *                secret, fabric_api_key)
     * @return task representing the token exchange process. Apps can listen to the status of the
     * returned task using {@link Task#addOnCompleteListener(Activity, OnCompleteListener)}
     */
    public Task<AuthResult> migrate(RedeemableDigitsSessionBuilder builder) {
        return firebaseAuth.signInWithCustomToken(storageHelpers.getUnsignedJWT(builder.build()
                .getPayload())).continueWithTask(new ClearSessionContinuation(storageHelpers));
    }

    /**
     * Migrates a user token from the Digits SDK, making that user the current user
     * in the new Firebase SDK. This is the recommended method that most apps would want to use.
     * <pre>
     * <code>AuthMigrator.getInstance().migrate().addOnCompleteListener(new{@code
     * OnCompleteListener<AuthResult>}() {
     *    {@literal @}Override
     *     public void onComplete({@code Task<AuthResult>}task) {
     *          if (task.isSuccessful()) {
     *              if (task.getResult().getUser() != null) {
     *                  String preservedDigitsPhoneNumber = task.getResult().getUser()
     *                      .getPhoneNumber();
     *                  String preservedDigitsId = task.getResult().getUser().getUid();
     *                  Log.d("Digits", "Preserved Phone Number" + preservedDigitsPhoneNumber);
     *                  Log.d("Digits", "Preserved User Id" + preservedDigitsId);
     *              } else {
     *                  //No valid digits session was found
     *                  Log.d("Digits", "No valid legacy digits session found");
     *              }
     *          } else {
     *              //an error accured
     *              Log.d("Digits", "Error while upgrading digits session: " + task.getException()
     *                  .getLocalizedMessage());
     *          }
     *     }
     * });</code></pre>
     * This method works as follows:
     * <ol><li>Looks up the legacy digits auth token.</li>
     * <li>Looks for the "io.fabric.ApiKey" in the app manifest</li>
     * <li>Looks for the ""com.digits.sdk.android.ConsumerKey"" in the appmanifest</li>
     * <li>Looks for the ""com.digits.sdk.android.ConsumerSecret"" in the app manifest</li>
     * <li>Sends the legacy token and keys to a Firebase server to exchange it for a new Firebase
     * auth token.</li>
     * <li>Uses the new auth token to log in the user.</li>
     * <li>Removes the legacy digits auth token from the device.</li></ol>
     * <p>
     * If a user is already logged in with the new Firebase SDK, then the
     * legacy auth token will be
     * removed, but the logged in user will not be affected.
     * <p>
     * If the Firebase server determines that the legacy auth token is invalid, it will be
     * removed and the user will not be logged in.
     *
     * @return task representing the token exchange process. Apps can listen to the status of the
     * returned task using {@link Task#addOnCompleteListener(Activity, OnCompleteListener)}
     */
    public Task<AuthResult> migrate() {
        String digitsSessionJson = storageHelpers.getDigitsSessionJson();
        return TextUtils.isEmpty(digitsSessionJson) ? Tasks.forResult((AuthResult) new
                MigratorAuthResult(null)) : migrate(digitsSessionJson);
    }

    /**
     * Checks whether an auth token from the legacy SDK exists.  Uses the FirebaseApp's name
     * (or 'default' for the default app) as the persistence key.
     *
     * @return does the client contain a legacy digits token
     */
    public boolean hasLegacyAuth() {
        return storageHelpers.hasDigitsSession();
    }

    /**
     * Clears the auth token from the legacy SDK.  Uses the FirebaseApp's name (or 'default' for
     * the default app) as the persistence key.
     */
    public void clearLegacyAuth() {
        storageHelpers.clearDigitsSession();
    }

    private AuthMigrator(FirebaseApp app, StorageHelpers storageHelper, FirebaseAuth firebaseAuth) {
        this.app = app;
        this.storageHelpers = storageHelper;
        this.firebaseAuth = firebaseAuth;
    }

    private Task<AuthResult> migrate(@NonNull String sessionJson) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // If there's already a current user, don't migrate and clear the legacy token.
        if (currentUser != null) {
            storageHelpers.clearDigitsSession();
            return Tasks.forResult((AuthResult) new MigratorAuthResult(currentUser));
        }

        final Context context = app.getApplicationContext();

        final RedeemableDigitsSessionBuilder builder;
        try {
            builder = RedeemableDigitsSessionBuilder.fromSessionJson(sessionJson).setConsumerKey
                    (storageHelpers.getApiKeyFromManifest(context, StorageHelpers
                            .DIGITS_CONSUMER_KEY_KEY)).setConsumerSecret(storageHelpers
                    .getApiKeyFromManifest(context, StorageHelpers.DIGITS_CONSUMER_SECRET_KEY))
                    .setFabricApiKey(storageHelpers.getApiKeyFromManifest(context, StorageHelpers
                            .FABRIC_API_KEY_KEY));
        } catch (JSONException e) {
            //invalid session
            storageHelpers.clearDigitsSession();
            return Tasks.forResult((AuthResult) new MigratorAuthResult(null));
        }

        return migrate(builder);
    }
}