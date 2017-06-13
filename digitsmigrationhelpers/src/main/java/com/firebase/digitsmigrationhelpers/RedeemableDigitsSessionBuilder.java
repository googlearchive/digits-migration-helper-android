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

package com.firebase.digitsmigrationhelpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.digitsmigrationhelpers.internal.ClearSessionContinuation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * Build a Digits session that can be exchanged for a user on the firebase SDK using the
 * {@link AuthMigrator}
 */
public class RedeemableDigitsSessionBuilder {
    private static final String TAG = "Digits";
    private Long id;
    private String phoneNumber;
    private String email;
    private Boolean isEmailVerified;
    private String authToken;
    private String authTokenSecret;
    private String consumerKey;
    private String consumerSecret;
    private String fabricApiKey;

    private static final String EMAIL_KEY = "email";
    private static final String PHONE_NUMBER_KEY = "phone_number";
    private static final String EMAIL_ADDRESS_KEY = "address";
    private static final String IS_EMAIL_ADDRESS_VERIFIED_KEY = "is_verified";
    private static final String AUTH_TOKEN_KEY = "auth_token";
    private static final String NESTED_TOKEN_KEY = "token";
    private static final String NESTED_TOKEN_SECRET_KEY = "secret";

    private static final String ID_KEY = "id";

    private static final String EXAMPLE_FABRIC_API_ENTRY =
            "\n<meta-data"
            + "\n\tandroid:name=\"io.fabric.ApiKey\""
            + "\n\tandroid:value=\"YOUR_FABRIC_API_KEY\""
            + "\n\ttools:replace=\"android:value\" />\n";
    private static final String EXAMPLE_CONSUMER_KEY_ENTRY =
            "\n<meta-data"
            + "\n\tandroid:name=\"com.digits.sdk.android.ConsumerKey\""
            + "\n\tandroid:value=\"YOUR_DIGITS_CONSUMER_KEY\""
            + "\n\ttools:replace=\"android:value\" />\n";
    private static final String EXAMPLE_CONSUMER_SECRET_ENTRY =
            "\n<meta-data" +
            "\n\tandroid:name=\"com.digits.sdk.android.ConsumerSecret\"" +
            "\n\tandroid:value=\"YOUR_DIGITS_CONSUMER_SECRET\"" +
            "\n\ttools:replace=\"android:value\" />\n";

    /**
     * Set digits user id
     *
     * @param id digits user id
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Set phone number
     *
     * @param phoneNumber digits user's phone number
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setPhoneNumber(@Nullable String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Set email address
     *
     * @param email digits user's email address
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setEmail(@Nullable String email) {
        this.email = email;
        return this;
    }

    /**
     * Set Is the email verified
     *
     * @param isEmailVerified whether the digits user's email was verified
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setIsEmailVerified(@Nullable Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
        return this;
    }

    /**
     * Set auth token issued for the user by the digits server
     *
     * @param authToken OAuth1a token issued to the digits user
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
        return this;
    }

    /**
     * Set secret issued for the user by the digits server
     *
     * @param authTokenSecret OAuth1a secret issued to the digits user
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setAuthTokenSecret(@Nullable String authTokenSecret) {
        this.authTokenSecret = authTokenSecret;
        return this;
    }

    /**
     * Set digits consumer key issued to the app at Fabric.io
     *
     * @param consumerKey consumer key issued to identify the app
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setConsumerKey(@Nullable String consumerKey) {
        this.consumerKey = consumerKey;
        return this;
    }

    /**
     * Set digits consumer secret issued to the app at Fabric.io
     *
     * @param consumerSecret consumer secret issued to authenticate the app
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setConsumerSecret(@Nullable String consumerSecret) {
        this.consumerSecret = consumerSecret;
        return this;
    }

    /**
     * Set Fabric api key issued to the app at Fabric.io
     *
     * @param fabricApiKey fabric api key
     * @return builder
     */
    public RedeemableDigitsSessionBuilder setFabricApiKey(@Nullable String fabricApiKey) {
        this.fabricApiKey = fabricApiKey;
        return this;
    }

    /**
     * Build {@link ClearSessionContinuation.RedeemableDigitsSession} using the parameters provided
     *
     * We permit null auth token and secret as permissible but corrupt tokens. Once these are
     * invalidated by the service, these are deleted from the client in
     * {@link ClearSessionContinuation}

     * @return redeemable digits session
     */
    public ClearSessionContinuation.RedeemableDigitsSession build() {
        checkNotNull(authToken, "Auth Token cannot be null");
        checkNotNull(authTokenSecret, "Token Secret cannot be null");

        failNotNull(consumerKey, "Consumer Key cannot be empty. "
                + "Your AndroidManifest.xml should have an entry like: "
                + EXAMPLE_CONSUMER_KEY_ENTRY);
        failNotNull(consumerSecret, "Consumer Secret cannot be empty. "
                + "Your AndroidManifest.xml should have an entry like:"
                + EXAMPLE_CONSUMER_SECRET_ENTRY);
        failNotNull(fabricApiKey, "Fabric Api Key cannot be empty. "
                + "Your AndroidManifest.xml should have an entry like: "
                + EXAMPLE_FABRIC_API_ENTRY);

        if(!isValidApiKeyFormat(fabricApiKey)) {
            throw new IllegalArgumentException("Invalid Fabric API key." +
                    "Contact support@fabric.io for assistance");
        }

        return new ClearSessionContinuation.RedeemableDigitsSession(id, phoneNumber, email, isEmailVerified, authToken,
                authTokenSecret, consumerKey, consumerSecret, fabricApiKey);
    }

    @NonNull
    static RedeemableDigitsSessionBuilder fromSessionJson(@NonNull String json) throws
            JSONException {
        RedeemableDigitsSessionBuilder builder = new RedeemableDigitsSessionBuilder();

        //Top level structures
        JSONObject jsonObject = new JSONObject(json);
        JSONObject emailJsonObject = safeGetJsonObject(EMAIL_KEY, jsonObject);
        JSONObject authTokenJsonObject = safeGetJsonObject(AUTH_TOKEN_KEY, jsonObject);

        builder.setPhoneNumber(safeGetString(PHONE_NUMBER_KEY, jsonObject));
        builder.setId(safeGetLong(ID_KEY, jsonObject));

        //Nested Structures
        JSONObject nestedAuthTokenJsonObject = safeGetJsonObject(AUTH_TOKEN_KEY,
                authTokenJsonObject);

        builder.setEmail(safeGetString(EMAIL_ADDRESS_KEY, emailJsonObject));
        builder.setIsEmailVerified(safeGetBoolean(IS_EMAIL_ADDRESS_VERIFIED_KEY, emailJsonObject));

        builder.setAuthToken(safeGetString(NESTED_TOKEN_KEY, nestedAuthTokenJsonObject));
        builder.setAuthTokenSecret(safeGetString(NESTED_TOKEN_SECRET_KEY,
                nestedAuthTokenJsonObject));

        return builder;

    }

    private static JSONObject safeGetJsonObject(String key, JSONObject jsonObject) throws
            JSONException {
        return safeHasKey(jsonObject, key) ? jsonObject.getJSONObject(key) : null;
    }

    private static String safeGetString(String key, JSONObject jsonObject) throws JSONException {
        return safeHasKey(jsonObject, key) ? jsonObject.getString(key) : null;
    }

    private static Long safeGetLong(String key, JSONObject jsonObject) throws JSONException {
        return safeHasKey(jsonObject, key) ? jsonObject.getLong(key) : null;
    }

    private static Boolean safeGetBoolean(String key, JSONObject jsonObject) throws JSONException {
        return safeHasKey(jsonObject, key) ? jsonObject.getBoolean(key) : null;
    }

    private static boolean safeHasKey(JSONObject jsonObject, String key) {
        return key != null && jsonObject != null && jsonObject.has(key);
    }

    private static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if(reference instanceof String && TextUtils.isEmpty((String) reference)) {
            Log.d(TAG, String.valueOf(errorMessage));
        }else if (reference == null) {
            Log.d(TAG, String.valueOf(errorMessage));
        }
        return reference;
    }

    private static <T> T failNotNull(T reference, @Nullable Object errorMessage) {
        if(reference instanceof String && TextUtils.isEmpty((String) reference)) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        } else if (reference == null) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
        return reference;
    }

    private static boolean isValidApiKeyFormat(String apiKey) {
        return apiKey != null
                && apiKey.length() == 40
                && Pattern.compile("[0-9a-f]+").matcher(apiKey).matches();
    }
}
