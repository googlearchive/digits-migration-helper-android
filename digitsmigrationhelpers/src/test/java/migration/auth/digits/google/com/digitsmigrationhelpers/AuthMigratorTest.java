package migration.auth.digits.google.com.digitsmigrationhelpers;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.digits.auth.migration.RobolectricGradleTestRunner;
import com.google.digits.auth.migration.internal.ClearSessionContinuation;
import com.google.digits.auth.migration.internal.RedeemableDigitsSession;
import com.google.digits.auth.migration.internal.StorageHelpers;
import com.google.firebase.FirebaseApp;
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AuthMigratorTest {
    @Mock
    FirebaseApp mockFirebaseApp;
    @Mock
    StorageHelpers mockStorageHelpers;
    @Mock
    FirebaseAuth mockFirebaseAuth;
    @Mock
    Task<AuthResult> mockAuthResultTask;
    @Captor
    private ArgumentCaptor<JSONObject> mjsonCaptor;
    @Mock
    private FirebaseUser mockFirebaseUser;

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
    private static final String FABRIC_API_KEY = "fabric_api_key";
    private static final String DIGITS_CONSUMER_KEY = "digits_consumer_key";
    private static final String DIGITS_CONSUMER_SECRET = "digits_consumer_secret";
    private static final String DIGITS_JWT = "digits_jwt";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockStorageHelpers.getApiKeyFromManifest(any(Context.class),
                eq(StorageHelpers.DIGITS_CONSUMER_KEY_KEY))).thenReturn(DIGITS_CONSUMER_KEY);
        when(mockStorageHelpers.getApiKeyFromManifest(any(Context.class),
                eq(StorageHelpers.DIGITS_CONSUMER_SECRET_KEY))).thenReturn(DIGITS_CONSUMER_SECRET);
        when(mockStorageHelpers.getApiKeyFromManifest(any(Context.class),
                eq(StorageHelpers.FABRIC_API_KEY_KEY))).thenReturn(FABRIC_API_KEY);
    }

    @Test
    public void migratewithoutTokens_noSession() {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(null);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);
        Task<AuthResult> task = authMigrator.migrate();
        assertTrue(task.isSuccessful());
        assertNull(task.getResult().getUser());
    }

    @Test
    public void migratewithoutTokens_invalidSession() {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn("invalid_session");

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);

        Task<AuthResult> task = authMigrator.migrate();
        assertTrue(task.isSuccessful());
        assertNull(task.getResult().getUser());
        verify(mockStorageHelpers).clearDigitsSession();
    }

    @Test
    public void migratewithoutTokens_foundFirebaseSession() throws JSONException {
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);

        Task<AuthResult> task = authMigrator.migrate();
        assertTrue(task.isSuccessful());
        assertEquals(mockFirebaseUser, task.getResult().getUser());
        verify(mockStorageHelpers).clearDigitsSession();
    }

    @Test
    public void migratewithoutTokens_validSession() throws JSONException {
        Task mockTask = mock(Task.class);
        when(mockStorageHelpers.getDigitsSessionJson()).thenReturn(VALID_DIGITS_SESSION);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(mockAuthResultTask);
        when(mockAuthResultTask.continueWithTask(any(ClearSessionContinuation.class))).thenReturn(mockTask);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);

        assertEquals(mockTask, authMigrator.migrate());
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);

        JSONObject jsonObject = mjsonCaptor.getValue();
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

    @Test
    public void migratewithTokens() throws JSONException {
        Task mockTask = mock(Task.class);
        when(mockStorageHelpers.getUnsignedJWT(mjsonCaptor.capture())).thenReturn(DIGITS_JWT);
        when(mockFirebaseAuth.signInWithCustomToken(DIGITS_JWT)).thenReturn(mockAuthResultTask);
        when(mockAuthResultTask.continueWithTask(any(ClearSessionContinuation.class))).thenReturn(mockTask);

        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);
        RedeemableDigitsSessionBuilder builder = new RedeemableDigitsSessionBuilder()
                .setAuthToken(AUTH_TOKEN).setAuthTokenSecret(AUTH_TOKEN_SECRET);

        assertEquals(mockTask, authMigrator.migrate(builder));
        verify(mockFirebaseAuth).signInWithCustomToken(DIGITS_JWT);

        JSONObject jsonObject = mjsonCaptor.getValue();
        assertTrue(jsonObject.isNull("id"));
        assertTrue(jsonObject.isNull("phone_number"));
        assertTrue(jsonObject.isNull("email_address"));
        assertTrue(jsonObject.isNull("is_email_verified"));
        assertTrue(jsonObject.isNull("app_consumer_key"));
        assertTrue(jsonObject.isNull("app_consumer_secret"));
        assertTrue(jsonObject.isNull("fabric_api_key"));
        assertEquals(AUTH_TOKEN, jsonObject.getString("auth_token"));
        assertEquals(AUTH_TOKEN_SECRET, jsonObject.getString("auth_token_secret"));
    }

    @Test
    public void hasLegacyAuth() throws JSONException {
        when(mockStorageHelpers.hasDigitsSession()).thenReturn(true);
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);

        assertTrue(authMigrator.hasLegacyAuth());
    }

    @Test
    public void clearLegacyAuth() throws JSONException {
        AuthMigrator authMigrator = new AuthMigrator(mockFirebaseApp,
                mockStorageHelpers,
                mockFirebaseAuth);

        authMigrator.clearLegacyAuth();
        verify(mockStorageHelpers).clearDigitsSession();
    }
}
