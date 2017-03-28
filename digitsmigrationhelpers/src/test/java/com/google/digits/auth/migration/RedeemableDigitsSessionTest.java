package com.google.digits.auth.migration;

import com.google.digits.auth.migration.internal.RedeemableDigitsSession;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import migration.auth.digits.google.com.digitsmigrationhelpers.BuildConfig;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RedeemableDigitsSessionTest {
    private static final long DIGITS_ID = 112L;
    private static final String PHONE_NUMBER = "1231";
    private static final String EMAIL = "a@b.com";
    private static final boolean IS_EMAIL_VERIFIED = false;
    private static final String AUTH_TOKEN = "auth_token";
    private static final String AUTH_TOKEN_SECRET = "auth_token_secret";
    private static final String CONSUMER_KEY = "consumer_key";
    private static final String CONSUMER_SECRET = "consumer_keys_secret";
    private static final String FABRIC_API_KEY = "fabric_api_key";

    @Test
    public void testInstanceWithNonNullValues() throws Exception {
        RedeemableDigitsSession session =
                new RedeemableDigitsSession(DIGITS_ID, PHONE_NUMBER, EMAIL,
                        IS_EMAIL_VERIFIED, AUTH_TOKEN, AUTH_TOKEN_SECRET, CONSUMER_KEY,
                        CONSUMER_SECRET, FABRIC_API_KEY);

        JSONObject jsonObject = session.getPayload();
        assertEquals(DIGITS_ID, jsonObject.getLong("id"));
        assertEquals(PHONE_NUMBER, jsonObject.getString("phone_number"));
        assertEquals(EMAIL, jsonObject.getString("email_address"));
        assertEquals(IS_EMAIL_VERIFIED, jsonObject.getBoolean("is_email_verified"));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
        assertEquals(CONSUMER_KEY, jsonObject.getString("app_consumer_key"));
        assertEquals(CONSUMER_SECRET, jsonObject.getString("app_consumer_secret"));
        assertEquals(FABRIC_API_KEY, jsonObject.getString("fabric_api_key"));
    }

    @Test
    public void testInstancesTokenWithNullValues() throws Exception {
        RedeemableDigitsSession session =
                new RedeemableDigitsSession(DIGITS_ID, PHONE_NUMBER, EMAIL,
                        IS_EMAIL_VERIFIED, AUTH_TOKEN, AUTH_TOKEN_SECRET, CONSUMER_KEY,
                        null, null);

        JSONObject jsonObject = session.getPayload();
        assertEquals(DIGITS_ID, jsonObject.getLong("id"));
        assertEquals(PHONE_NUMBER, jsonObject.getString("phone_number"));
        assertEquals(EMAIL, jsonObject.getString("email_address"));
        assertEquals(IS_EMAIL_VERIFIED, jsonObject.getBoolean("is_email_verified"));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
        assertEquals(CONSUMER_KEY, jsonObject.getString("app_consumer_key"));
        assertTrue(jsonObject.isNull("app_consumer_secret"));
        assertTrue(jsonObject.isNull("fabric_api_key"));
    }
}