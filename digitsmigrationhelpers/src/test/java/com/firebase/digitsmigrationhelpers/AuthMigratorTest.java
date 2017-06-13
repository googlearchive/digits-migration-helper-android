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

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.firebase.digitsmigrationhelpers.internal.FirebaseWebRequestException;
import com.firebase.digitsmigrationhelpers.internal.StorageHelpers;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import migration.auth.digits.google.com.digitsmigrationhelpers.BuildConfig;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AuthMigratorTest {
    private static final long DIGITS_ID = 112L;
    private static final String EMAIL = "a@b.com";
    private static final boolean IS_EMAIL_VERIFIED = false;
    private static final String AUTH_TOKEN = "auth_token";
    private static final String AUTH_TOKEN_SECRET = "auth_token_secret";
    private static final String PHONE_NUMBER = "5551234567";
    private static final String VALID_DIGITS_SESSION = String.format
            ("{\"email\":{\"address\":\"%s\",\"is_verified\":%b},\"phone_number\":\"%s\"," +
                    "\"auth_token\":{\"auth_type\":\"oauth1a\",\"auth_token\":{\"secret\":\"%s\"," +
                    "" + "\"token\":\"%s\",\"created_at\":0}},\"id\":%d}", EMAIL,
                    IS_EMAIL_VERIFIED, PHONE_NUMBER, AUTH_TOKEN_SECRET, AUTH_TOKEN, DIGITS_ID);
    private static final String DIGITS_CONSUMER_KEY = "digits_consumer_key";
    private static final String DIGITS_CONSUMER_SECRET = "digits_consumer_secret";
    private static final String DIGITS_JWT = "digits_jwt";
    private static final String FABRIC_API_KEY = "abcdefabcdefabcdefabcdefabcdefabcdefabcd";

    @Mock
    FirebaseApp mockFirebaseApp;
    @Mock
    StorageHelpers mockStorageHelpers;
    @Mock
    FirebaseAuth mockFirebaseAuth;
    @Captor
    private ArgumentCaptor<JSONObject> mjsonCaptor;
    @Mock
    private FirebaseUser mockFirebaseUser;
    private AuthResult authResult;
    private Task<AuthResult> authResultTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockStorageHelpers.getApiKeyFromManifest(any(Context.class), eq(StorageHelpers
                .DIGITS_CONSUMER_KEY_KEY))).thenReturn(DIGITS_CONSUMER_KEY);
        when(mockStorageHelpers.getApiKeyFromManifest(any(Context.class), eq(StorageHelpers
                .DIGITS_CONSUMER_SECRET_KEY))).thenReturn(DIGITS_CONSUMER_SECRET);
        when(mockStorageHelpers.getApiKeyFromManifest(any(Context.class), eq(StorageHelpers
                .FABRIC_API_KEY_KEY))).thenReturn(FABRIC_API_KEY);
        authResult = new AuthResult() {
            @Override
            public FirebaseUser getUser() {
                return mockFirebaseUser;
            }

            @Override
            public AdditionalUserInfo getAdditionalUserInfo() {
                return null;
            }
        };

        authResultTask = Tasks.forResult(authResult);
    }

    @Test
    public void migrateAndClear_tokenFound() throws JSONException {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(authResultTask);
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        assertTrue(authMigrator.migrate(true).isSuccessful());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);
        verify(mockStorageHelpers).clearDigitsSession();
        checkCompleteJsonObject(mjsonCaptor.getValue());
    }

    @Test
    public void migrateAndClear_badSessionResponse() throws JSONException {
        Task<AuthResult> task = Tasks.forException(new FirebaseWebRequestException("msg", 400));

        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(task);
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        assertFalse(authMigrator.migrate(true).isSuccessful());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);
        verify(mockStorageHelpers).clearDigitsSession();
        checkCompleteJsonObject(mjsonCaptor.getValue());
    }

    @Test
    public void migrateAndClear_unauthorizedSessionResponse() throws JSONException {
        Task<AuthResult> task = Tasks.forException(new FirebaseWebRequestException("msg", 403));

        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(task);
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        assertFalse(authMigrator.migrate(true).isSuccessful());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);
        verify(mockStorageHelpers).clearDigitsSession();
        checkCompleteJsonObject(mjsonCaptor.getValue());
    }

    @Test
    public void migrateAndKeep_unauthorizedSessionResponse() throws JSONException {
        Task<AuthResult> task = Tasks.forException(new FirebaseWebRequestException("msg", 403));

        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(task);
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        assertFalse(authMigrator.migrate(false).isSuccessful());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);
        verify(mockStorageHelpers, times(0)).clearDigitsSession();
        checkCompleteJsonObject(mjsonCaptor.getValue());
    }

    @Test
    public void migrateAndKeep_tokenFound() throws JSONException {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(authResultTask);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);

        assertTrue(authMigrator.migrate(false).isSuccessful());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);
        verify(mockStorageHelpers, times(0)).clearDigitsSession();
        checkCompleteJsonObject(mjsonCaptor.getValue());
    }

    @Test
    public void migrateAndClear_foundFirebaseSession() throws JSONException {
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);

        assertTrue(authMigrator.migrate(true).isSuccessful());
        verify(mockStorageHelpers).clearDigitsSession();
        verify(mockFirebaseAuth, times(0)).signInWithCustomToken(any(String.class));
    }

    @Test
    public void migrateAndKeep_foundFirebaseSession() throws JSONException {
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        assertTrue(authMigrator.migrate(false).isSuccessful());
        verify(mockStorageHelpers, times(0)).clearDigitsSession();
        verify(mockFirebaseAuth, times(0)).signInWithCustomToken(any(String.class));
    }

    @Test
    public void migrate_noLegacyToken() {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(null);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        assertTrue(authMigrator.migrate(true).isSuccessful());
        verify(mockFirebaseAuth, times(0)).signInWithCustomToken(any(String.class));
    }

    @Test
    public void migrateAndClear_invalidToken() {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn("invalid_session");

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);

        assertTrue(authMigrator.migrate(true).isSuccessful());
        verify(mockStorageHelpers).clearDigitsSession();
        verify(mockFirebaseAuth, times(0)).signInWithCustomToken(any(String.class));
    }

    @Test
    public void migrateAndKeep_invalidToken() {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn("invalid_session");

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);

        assertTrue(authMigrator.migrate(false).isSuccessful());
        verify(mockStorageHelpers,times(0)).clearDigitsSession();
        verify(mockFirebaseAuth, times(0)).signInWithCustomToken(any(String.class));
    }

    @Test
    public void migrateCustomSession() throws JSONException {
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(authResultTask);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);
        RedeemableDigitsSessionBuilder builder = new RedeemableDigitsSessionBuilder()
                .setAuthToken(AUTH_TOKEN)
                .setAuthTokenSecret(AUTH_TOKEN_SECRET)
                .setConsumerKey(DIGITS_CONSUMER_KEY)
                .setConsumerSecret(DIGITS_CONSUMER_SECRET)
                .setFabricApiKey(FABRIC_API_KEY);

        assertTrue(authMigrator.migrate(builder).isSuccessful());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);

        JSONObject jsonObject = mjsonCaptor.getValue();
        assertTrue(jsonObject.isNull("id"));
        assertTrue(jsonObject.isNull("phone_number"));
        assertTrue(jsonObject.isNull("email_address"));
        assertTrue(jsonObject.isNull("is_email_verified"));
        assertTrue(jsonObject.isNull(DIGITS_CONSUMER_KEY));
        assertTrue(jsonObject.isNull(DIGITS_CONSUMER_SECRET));
        assertTrue(jsonObject.isNull(FABRIC_API_KEY));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
    }

    @Test
    public void hasLegacyAuth() throws JSONException {
        when(mockStorageHelpers.hasDigitsSession()).thenReturn(true);
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);

        assertTrue(authMigrator.hasLegacyAuth());
    }

    @Test
    public void clearLegacyAuth() throws JSONException {
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp, mockStorageHelpers,
                mockFirebaseAuth);

        authMigrator.clearLegacyAuth();
        verify(mockStorageHelpers).clearDigitsSession();
    }

    private void checkCompleteJsonObject(JSONObject jsonObject) throws JSONException {
        assertEquals(DIGITS_ID, jsonObject.getLong("id"));
        assertEquals(PHONE_NUMBER, jsonObject.getString("phone_number"));
        assertEquals(EMAIL, jsonObject.getString("email_address"));
        assertEquals(IS_EMAIL_VERIFIED, jsonObject.getBoolean("is_email_verified"));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
        assertEquals(DIGITS_CONSUMER_KEY, jsonObject.getString("app_consumer_key"));
        assertEquals(DIGITS_CONSUMER_SECRET, jsonObject.getString("app_consumer_secret"));
        assertEquals(FABRIC_API_KEY, jsonObject.getString("fabric_api_key"));
    }
}
