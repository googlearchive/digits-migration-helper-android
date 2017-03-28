package com.google.digits.auth.migration;

import com.google.digits.auth.migration.internal.RedeemableDigitsSessionBuilder;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE)
public class RedeemableDigitsSessionBuilderTest {

    private static final long DIGITS_ID = 112L;
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
                RedeemableDigitsSessionBuilder.fromSessionJson(VALID_DIGITS_SESSION);
        JSONObject jsonObject = builder.build().getPayload();

        assertEquals(DIGITS_ID, jsonObject.getLong("id"));
        assertEquals(PHONE_NUMBER, jsonObject.getString("phone_number"));
        assertEquals(EMAIL, jsonObject.getString("email_address"));
        assertEquals(IS_EMAIL_VERIFIED, jsonObject.getBoolean("is_email_verified"));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
    }

    @Test
    public void testFromSessionJson_withNullFields() throws JSONException {
        RedeemableDigitsSessionBuilder builder =
                RedeemableDigitsSessionBuilder.fromSessionJson("{}");
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
}