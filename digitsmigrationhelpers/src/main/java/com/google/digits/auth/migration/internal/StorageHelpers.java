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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.Charset;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class StorageHelpers {
    @Nullable
    private final SharedPreferences mDigitsSharedPreferences;
    @NonNull
    public static final String DIGITS_SESSION_PREF_FILE_NAME =
            "com.digits.sdk.android:digits:session_store";
    @NonNull
    public static final String DIGITS_PREF_KEY_ACTIVE_SESSION = "active_session";
    @NonNull
    public static final String FABRIC_API_KEY_KEY = "io.fabric.ApiKey";
    @NonNull
    public static final String DIGITS_CONSUMER_KEY_KEY = "com.digits.sdk.android.ConsumerKey";
    @NonNull
    public static final String DIGITS_CONSUMER_SECRET_KEY = "com.digits.sdk.android.ConsumerSecret";
    @NonNull
    private static final String BASE_64_NONE_ALGORITHM_JWT_HEADER = "eyJhbGciOiJub25lIn0=";
    @NonNull
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
    public String getApiKeyFromManifest(@NonNull Context context, @NonNull String key) {
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

    @NonNull
    public String getUnsignedJWT(@NonNull JSONObject payload) {
        return BASE_64_NONE_ALGORITHM_JWT_HEADER
                + "."
                + Base64.encodeToString(payload.toString().getBytes(Charset.forName("UTF-8")),
                Base64.DEFAULT)
                + ".";
    }
}
