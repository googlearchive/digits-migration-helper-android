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

package com.firebase.digitsmigrationhelpers.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ClearSessionContinuation implements Continuation<Void, Task<Void>> {

    private final StorageHelpers storageHelpers;

    public ClearSessionContinuation(StorageHelpers storageHelpers) {
        this.storageHelpers = storageHelpers;
    }

    @Override
    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
        if (!task.isSuccessful()) {
            try {
                throw task.getException();
            } catch (FirebaseWebRequestException e) {
                if (e.getHttpStatusCode() == 400 || e.getHttpStatusCode() == 403) {
                    Log.d(TAG, "Digits session deemed invalid by server");
                    Log.d(TAG, "Clearing legacy session");
                    // Permanent errors should clear the persistence key.
                    storageHelpers.clearDigitsSession();
                }
                return task;
            }
        }
        storageHelpers.clearDigitsSession();
        return task;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static class RedeemableDigitsSession {

        @NonNull
        private final String authToken;

        @NonNull
        private final String authTokenSecret;

        @NonNull
        private final String consumerKey;

        @NonNull
        private final String consumerSecret;

        @NonNull
        private final String fabricApiKey;

        @Nullable
        private final String email;

        @Nullable
        private final Boolean isEmailVerified;

        @Nullable
        private final String phoneNumber;

        @Nullable
        private final Long id;

        public RedeemableDigitsSession(@Nullable Long id, @Nullable String phoneNumber,
                                       @Nullable String email, @Nullable Boolean isEmailVerified,
                                       @NonNull String authToken, @NonNull String authTokenSecret,
                                       @NonNull String consumerKey, @NonNull String consumerSecret,
                                       @NonNull String fabricApiKey) {
            this.id = id;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.isEmailVerified = isEmailVerified;
            this.authToken = authToken;
            this.authTokenSecret = authTokenSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.fabricApiKey = fabricApiKey;
        }

        @NonNull
        public JSONObject getPayload() {
            JSONObject jsonObject = new JSONObject();
            try {
                //Safe to insert null values
                jsonObject.put("id", id);
                jsonObject.put("phone_number", phoneNumber);
                jsonObject.put("email_address", email);
                jsonObject.put("is_email_verified", isEmailVerified);
                jsonObject.put("auth_token", authToken);
                jsonObject.put("auth_token_secret", authTokenSecret);
                jsonObject.put("app_consumer_key", consumerKey);
                jsonObject.put("app_consumer_secret", consumerSecret);
                jsonObject.put("fabric_api_key", fabricApiKey);

                return jsonObject;
            } catch (JSONException e) {
                return jsonObject;
            }
        }
    }
}