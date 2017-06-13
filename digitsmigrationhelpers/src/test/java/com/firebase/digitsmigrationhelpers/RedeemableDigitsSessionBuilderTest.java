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

package com.firebase.digitsmigrationhelpers;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE)
public class RedeemableDigitsSessionBuilderTest {

    private static final long DIGITS_ID = 112L;
    private static final String FABRIC_API_KEY = "abcdefabcdefabcdefabcdefabcdefabcdefabcd";
    private static final String CONSUMER_KEY = "abcdefabcdefabcdefabcdefabcdefabcdefabcd";
    private static final String CONSUMER_SECRET = "abcdefabcdefabcdefabcdefabcdefabcdefabcd";
    private static final String EMAIL = "a@b.com";
    private static final boolean IS_EMAIL_VERIFIED = false;
    private static final String AUTH_TOKEN = "auth_token";
    private static final String AUTH_TOKEN_SECRET = "auth_token_secret";
    private static final String PHONE_NUMBER = "5551234567";
    private static final String VALID_DIGITS_SESSION =
            String.format("{\"email\":{\"address\":\"%s\",\"is_verified\":%b},\"phone_number\":\"%s\","
                            + "\"auth_token\":{\"auth_type\":\"oauth1a\",\"auth_token\":{\"secret\":\"%s\","
                            + "\"token\":\"%s\",\"created_at\":0}},\"id\":%d}",
                    EMAIL, IS_EMAIL_VERIFIED, PHONE_NUMBER, AUTH_TOKEN_SECRET, AUTH_TOKEN, DIGITS_ID);

    @Test
    public void testFromSessionJson_allFields() throws JSONException {
        RedeemableDigitsSessionBuilder builder =
                RedeemableDigitsSessionBuilder.fromSessionJson(VALID_DIGITS_SESSION)
                        .setConsumerKey(CONSUMER_KEY)
                        .setConsumerSecret(CONSUMER_SECRET)
                        .setFabricApiKey(FABRIC_API_KEY);
        JSONObject jsonObject = builder.build().getPayload();

        assertEquals(DIGITS_ID, jsonObject.getLong("id"));
        assertEquals(PHONE_NUMBER, jsonObject.getString("phone_number"));
        assertEquals(EMAIL, jsonObject.getString("email_address"));
        assertEquals(IS_EMAIL_VERIFIED, jsonObject.getBoolean("is_email_verified"));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
    }

    @Test
    public void testFromSessionJson_withNullableFields() throws JSONException {
        RedeemableDigitsSessionBuilder builder =
                RedeemableDigitsSessionBuilder.fromSessionJson("{}")
                        .setConsumerKey(CONSUMER_KEY)
                        .setConsumerSecret(CONSUMER_SECRET)
                        .setFabricApiKey(FABRIC_API_KEY);
        JSONObject jsonObject = builder.build().getPayload();

        assertTrue(jsonObject.isNull("id"));
        assertTrue(jsonObject.isNull("phone_number"));
        assertTrue(jsonObject.isNull("email_address"));
        assertTrue(jsonObject.isNull("is_email_verified"));
        assertTrue(jsonObject.isNull("auth_token"));
        assertTrue(jsonObject.isNull("auth_token_secret"));
    }

    @Test(expected=JSONException.class)
    public void testInvalidFromDigitsSessionJson() throws JSONException {
        RedeemableDigitsSessionBuilder.fromSessionJson("invalid_json");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoConsumerKey() throws JSONException {
        RedeemableDigitsSessionBuilder.fromSessionJson(VALID_DIGITS_SESSION)
                .setConsumerSecret("")
                .setFabricApiKey(FABRIC_API_KEY).build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoConsumerSecret() throws JSONException {
        RedeemableDigitsSessionBuilder.fromSessionJson(VALID_DIGITS_SESSION)
                .setConsumerKey("")
                .setFabricApiKey(FABRIC_API_KEY).build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoFabricApiKey() throws JSONException {
        RedeemableDigitsSessionBuilder.fromSessionJson(VALID_DIGITS_SESSION)
                .setConsumerKey("")
                .setConsumerSecret("").build();
    }
}