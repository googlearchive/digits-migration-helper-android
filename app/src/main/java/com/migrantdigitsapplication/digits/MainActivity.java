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

package com.migrantdigitsapplication.digits;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import migration.auth.digits.google.com.digitsmigrationhelpers.AuthMigrator;
import migration.auth.digits.google.com.migrantdigitsapplication.R;

public class MainActivity extends AppCompatActivity {
    private Button upgradeCustomTokenButton;
    private Button upgradeDefaultSessionButton;
    private EditText digitsAuthTokenEditText;
    private EditText digitsAuthTokenSecretEditText;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "DigitsMigration";
    private AuthMigrator migrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        migrator = AuthMigrator.getInstance();

        digitsAuthTokenEditText = (EditText) findViewById(R.id.digits_auth_token_edit_text);
        digitsAuthTokenSecretEditText = (EditText) findViewById(R.id.digits_auth_token_secret_edit_text);
        upgradeCustomTokenButton = (Button) findViewById(R.id.set_digits_session_button);
        upgradeDefaultSessionButton = (Button)findViewById(R.id.upgrade_digits_session_nutton);

        setupUpgradeCustomTokenButton();
        setupUpgradeDefaultSessionButton();
        setupFirebaseAuthListener();
    }

    private void setupFirebaseAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(getApplicationContext(),
                            "onAuthStateChanged:signed_in:" + user.getUid(), Toast.LENGTH_LONG)
                            .show();
                } else {
                    // User is signed out
                    Toast.makeText(getApplicationContext(),
                            "onAuthStateChanged:signed_out", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void setupUpgradeDefaultSessionButton() {
        if(migrator.hasLegacyAuth()) {
            upgradeDefaultSessionButton.setEnabled(true);
        } else {
            upgradeDefaultSessionButton.setEnabled(false);
        }

        upgradeDefaultSessionButton.setOnClickListener(
                new UpgradeDefaultSessionButtonListener(migrator));
    }

    private void setupUpgradeCustomTokenButton() {
        upgradeCustomTokenButton.setOnClickListener(
                new UpgradeCustomTokenButtonListener(
                        this,
                        migrator,
                        digitsAuthTokenEditText,
                        digitsAuthTokenSecretEditText
                )
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
