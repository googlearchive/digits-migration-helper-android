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

package com.google.digits.auth.migration.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import org.json.JSONException;
import org.json.JSONObject;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class RedeemableDigitsSession {

    @NonNull
    final String authToken;

    @NonNull
    final String authTokenSecret;

    @NonNull
    final String phoneNumber;

    @NonNull
    final Long id;

    @Nullable
    final String email;

    @Nullable
    final Boolean isEmailVerified;

    @Nullable
    final String consumerKey;

    @Nullable
    final String consumerSecret;

    @Nullable
    final String fabricApiKey;

    public RedeemableDigitsSession(Long id, @NonNull String phoneNumber, String email,
                                   Boolean isEmailVerified, @NonNull String authToken,
                                   @NonNull String authTokenSecret, String consumerKey,
                                   String consumerSecret, String fabricApiKey) {
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