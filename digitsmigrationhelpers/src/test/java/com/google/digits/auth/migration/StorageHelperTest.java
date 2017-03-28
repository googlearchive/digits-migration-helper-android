package com.google.digits.auth.migration;


import static com.google.digits.auth.migration.internal.StorageHelpers.DIGITS_PREF_KEY_ACTIVE_SESSION;
import static com.google.digits.auth.migration.internal.StorageHelpers.DIGITS_SESSION_PREF_FILE_NAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.google.digits.auth.migration.internal.StorageHelpers;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE)
public class StorageHelperTest {
    @Mock
    private Context mMockContext;
    @Mock
    private SharedPreferences mMockSharedPrefs;
    @Mock
    private SharedPreferences mMockDigitsSharedPrefs;
    @Mock
    Editor mMockEditor;
    @Mock
    PackageManager mMockPackageManager;
    ApplicationInfo mMockApplicationInfo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mMockApplicationInfo = new ApplicationInfo();
    }

    @Test
    public void testGetDigitsSessionJsonWithDigitsSharedPrefs() {
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getSharedPreferences(DIGITS_SESSION_PREF_FILE_NAME, Context.MODE_PRIVATE))
                .thenReturn(mMockDigitsSharedPrefs);
        when(mMockDigitsSharedPrefs.getString(DIGITS_PREF_KEY_ACTIVE_SESSION, null))
                .thenReturn("session_json");

        StorageHelpers helpers = new StorageHelpers(mMockContext);
        assertEquals("session_json", helpers.getDigitsSessionJson());
    }

    @Test
    public void testGetDigitsSessionJsonWithoutDigitsSharedPrefs() {
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getSharedPreferences(DIGITS_SESSION_PREF_FILE_NAME, Context.MODE_PRIVATE))
                .thenReturn(null);

        StorageHelpers helpers = new StorageHelpers(mMockContext);
        assertNull(helpers.getDigitsSessionJson());
    }

    @Test
    public void testClearDigitsSession() {
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getSharedPreferences(DIGITS_SESSION_PREF_FILE_NAME, Context.MODE_PRIVATE))
                .thenReturn(mMockDigitsSharedPrefs);
        when(mMockDigitsSharedPrefs.edit()).thenReturn(mMockEditor);
        when(mMockEditor.clear()).thenReturn(mMockEditor);

        StorageHelpers helpers = new StorageHelpers(mMockContext);
        helpers.clearDigitsSession();
        verify(mMockEditor).clear();
        verify(mMockEditor).apply();
    }

    @Test
    public void testGetUnsignedJWT() throws JSONException {
        String expectedJWT = "eyJhbGciOiJub25lIn0=.eyJrZXkiOiJ2YWx1ZSJ9\n.";
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getSharedPreferences(DIGITS_SESSION_PREF_FILE_NAME, Context.MODE_PRIVATE))
                .thenReturn(mMockDigitsSharedPrefs);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");

        StorageHelpers helpers = new StorageHelpers(mMockContext);
        assertEquals(expectedJWT, helpers.getUnsignedJWT(jsonObject));
    }

    @Test
    public void testGetApiKeyFromManifestWithKeysAvailable()
            throws JSONException, NameNotFoundException {
        when(mMockContext.getApplicationContext()).thenReturn(mMockContext);
        when(mMockContext.getSharedPreferences(DIGITS_SESSION_PREF_FILE_NAME, Context.MODE_PRIVATE))
                .thenReturn(mMockDigitsSharedPrefs);
        when(mMockContext.getPackageName()).thenReturn("package_name");
        when(mMockContext.getPackageManager()).thenReturn(mMockPackageManager);
        when(mMockPackageManager.getApplicationInfo("package_name", PackageManager.GET_META_DATA))
                .thenReturn(mMockApplicationInfo);

        Bundle bundle = new Bundle();
        bundle.putString("api_key", "value");
        mMockApplicationInfo.metaData = bundle;

        StorageHelpers helpers = new StorageHelpers(mMockContext);
        assertEquals("value", helpers.getApiKeyFromManifest(mMockContext, "api_key"));
    }
}