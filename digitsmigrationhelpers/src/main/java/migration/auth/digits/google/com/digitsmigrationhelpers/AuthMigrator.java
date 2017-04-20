/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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
 * Firebase SDK.
 */
public final class AuthMigrator {
    private static final WeakHashMap<FirebaseApp, AuthMigrator> instances = new WeakHashMap<>();
    private final StorageHelpers storageHelpers;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseApp app;

    /**
     * Gets an instance of {@link AuthMigrator}
     * @param app
     * @return
     */
    public static AuthMigrator getInstance(FirebaseApp app) {
        synchronized (instances) {
            AuthMigrator instance = instances.get(app);
            if (instance == null) {
                instance = new AuthMigrator(
                        app,
                        new StorageHelpers(app.getApplicationContext()),
                        FirebaseAuth.getInstance());
                instances.put(app, instance);
            }
            return instance;
        }
    }

    /**
     * Gets an instance of the {@link AuthMigrator} with the app returned by
     * {@link FirebaseApp#getInstance()}
     * @return
     */
    public static AuthMigrator getInstance() {
        return getInstance(FirebaseApp.getInstance());
    }


    /**
     * gets the {@link FirebaseApp} for this {@link AuthMigrator} instance
     * @return
     */
    public FirebaseApp getApp() {
        return app;
    }

    /**
     * Migrates a user token from the Digits SDK, making that user the current user
     * in the new Firebase SDK.
     *
     * This works as follows:
     * <ol>
     *     <li>Sends the legacy token and keys provided by the caller in the
     *     {@link RedeemableDigitsSessionBuilder} to a Firebase server to exchange it for a new
     *     Firebase auth token.</li>
     *     <li>Uses the new auth token to log in the user.</li>
     *     <li>Removes the legacy digits auth token from the device.</li>
     * </ol>
     *
     * If a user is already logged in with the new Firebase SDK, then the legacy auth token will be
     * removed, but the logged in user will not be affected.
     *
     * If the Firebase server determines that the legacy auth token is invalid, it will be removed
     * and the user will not be logged in.
     * @param builder
     * @return
     */
    public Task<AuthResult> migrate(RedeemableDigitsSessionBuilder builder) {
        return firebaseAuth.signInWithCustomToken(
                storageHelpers.getUnsignedJWT(builder.build().getPayload())
        ).continueWithTask(new ClearSessionContinuation(storageHelpers));
    }

    /**
     * Migrates a user token from the Digits SDK, making that user the current user
     * in the new Firebase SDK.
     *
     * This works as follows:
     * <ol>
     *     <li>Looks up the legacy digits auth token.</li>
     *     <li>Looks for the "io.fabric.ApiKey" in the app manifest</li>
     *     <li>Looks for the ""com.digits.sdk.android.ConsumerKey"" in the app manifest</li>
     *     <li>Looks for the ""com.digits.sdk.android.ConsumerSecret"" in the app manifest</li>
     *     <li>Sends the legacy token and keys to a Firebase server to exchange it for a new
     *     Firebase auth token.</li>
     *     <li>Uses the new auth token to log in the user.</li>
     *     <li>Removes the legacy digits auth token from the device.</li>
     * </ol>
     *
     * If a user is already logged in with the new Firebase SDK, then the legacy auth token will be
     * removed, but the logged in user will not be affected.
     *
     * If the Firebase server determines that the legacy auth token is invalid, it will be removed
     * and the user will not be logged in.
     * @return
     */
    public Task<AuthResult> migrate() {
        String digitsSessionJson = storageHelpers.getDigitsSessionJson();
        return TextUtils.isEmpty(digitsSessionJson)
                ? Tasks.forResult((AuthResult)new MigratorAuthResult(null))
                : migrate(digitsSessionJson);
    }

    /**
     * Checks whether an auth token from the legacy SDK exists.  Uses the FirebaseApp's name
     * (or 'default' for the default app) as the persistence key.
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

    AuthMigrator(FirebaseApp app, StorageHelpers storageHelper, FirebaseAuth firebaseAuth) {
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
            builder = RedeemableDigitsSessionBuilder
                    .fromSessionJson(sessionJson)
                    .setConsumerKey(storageHelpers
                        .getApiKeyFromManifest(context, StorageHelpers.DIGITS_CONSUMER_KEY_KEY))
                    .setConsumerSecret(storageHelpers
                        .getApiKeyFromManifest(context, StorageHelpers.DIGITS_CONSUMER_SECRET_KEY))
                    .setFabricApiKey(storageHelpers
                        .getApiKeyFromManifest(context, StorageHelpers.FABRIC_API_KEY_KEY));
        } catch (JSONException e) {
            //invalid session
            storageHelpers.clearDigitsSession();
            return Tasks.forResult((AuthResult)new MigratorAuthResult(null));
        }

        return migrate(builder);
    }
}