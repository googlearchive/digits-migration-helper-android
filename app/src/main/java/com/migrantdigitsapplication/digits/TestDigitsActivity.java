package com.migrantdigitsapplication.digits;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;
import migration.auth.digits.google.com.migrantdigitsapplication.R;

// This activity can be used to create a digits session before testing the auth migrator.
// Switch out the main Launcher activity in AndroidManifest.xml
public class TestDigitsActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this,
                new TwitterCore(
                        new TwitterAuthConfig(
                                getString(R.string.CONSUMER_KEY),
                                getString(R.string.CONSUMER_SECRET))),
                new Digits.Builder().build());

        Digits.authenticate(new AuthConfig.Builder().withAuthCallBack(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                Toast.makeText(TestDigitsActivity.this, "Digits sign in success " + phoneNumber,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(DigitsException error) {
                Toast.makeText(TestDigitsActivity.this, "Digits sign in failed ", Toast.LENGTH_LONG)
                        .show();
            }
        }).build());
    }
}
