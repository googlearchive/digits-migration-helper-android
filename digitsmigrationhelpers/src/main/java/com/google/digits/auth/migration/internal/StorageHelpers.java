package com.google.digits.auth.migration.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.Charset;

/**
 * @hide
 */
public class StorageHelpers {
    private final SharedPreferences mDigitsSharedPreferences;
    public static final String DIGITS_SESSION_PREF_FILE_NAME =
            "com.digits.sdk.android:digits:session_store";
    public static final String DIGITS_PREF_KEY_ACTIVE_SESSION = "active_session";
    public static final String FABRIC_API_KEY_KEY = "io.fabric.ApiKey";
    public static final String DIGITS_CONSUMER_KEY_KEY = "com.digits.sdk.android.ConsumerKey";
    public static final String DIGITS_CONSUMER_SECRET_KEY = "com.digits.sdk.android.ConsumerSecret";
    public static final String BASE_64_NONE_ALGORITHM_JWT_HEADER = "eyJhbGciOiJub25lIn0=";
    private static final String TAG = "DigitsMigrationhelpers";

    public StorageHelpers(@NonNull Context context) {
        mDigitsSharedPreferences = context.getApplicationContext()
                .getSharedPreferences(DIGITS_SESSION_PREF_FILE_NAME, Context.MODE_PRIVATE);

    }

    @Nullable
    public String getDigitsSessionJson() {
        return mDigitsSharedPreferences == null
                ? null
                : mDigitsSharedPreferences.getString(DIGITS_PREF_KEY_ACTIVE_SESSION, null);
    }

    @Nullable
    public String getApiKeyFromManifest(Context context, String key) {
        String apiKey = null;
        try {
            final String packageName = context.getPackageName();
            final ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            final Bundle bundle = ai.metaData;
            if (bundle != null) {
                apiKey = bundle.getString(key);
            }
        } catch (Exception e) {
            Log.w(TAG, key + "not found in manifest. Add it maybe?");
        }
        return apiKey;
    }


    public void clearDigitsSession() {
        if (mDigitsSharedPreferences != null) {
            mDigitsSharedPreferences.edit().clear().apply();
        }
    }

    public boolean hasDigitsSession() {
        return (mDigitsSharedPreferences != null)
                &&
                ! TextUtils.isEmpty(
                        mDigitsSharedPreferences.getString(DIGITS_PREF_KEY_ACTIVE_SESSION, null)
                );
    }

    @Nullable
    public String getUnsignedJWT(@NonNull JSONObject payload) {
        return BASE_64_NONE_ALGORITHM_JWT_HEADER
                + "."
                + Base64.encodeToString(payload.toString().getBytes(Charset.forName("UTF-8")),
                Base64.DEFAULT)
                + ".";
    }
}
