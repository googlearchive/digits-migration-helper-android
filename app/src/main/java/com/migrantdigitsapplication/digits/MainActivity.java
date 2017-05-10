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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;
import java.util.List;

import migration.auth.digits.google.com.digitsmigrationhelpers.AuthMigrator;

public class MainActivity extends AppCompatActivity {
    private AuthMigrator migrator;
    private Task<AuthResult> digitsMigratorTask;
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = MainActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        digitsMigratorTask = MigrantDigitsApplication.get(this).getDigitsMigratorTask();
        digitsMigratorTask.addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if (authResult.getUser() != null) {
                    // Either a user was already logged in or token exchange succeeded
                    startLoggedInUX();
                } else {
                    // No tokens were found to exchange and no firebase user logged in.
                    startLoginWithFirebaseUI();
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error migrating digits token
                startLoginWithFirebaseUI();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RC_SIGN_IN) {
            Toast.makeText(this, "Signed in using firebae ui", Toast.LENGTH_LONG);
            startLoggedInUX();
        }
    }

    private void startLoggedInUX() {
        Intent intent = new Intent(this, LoggedInActivity.class);
        startActivity(intent);
    }

    private void startLoginWithFirebaseUI() {
        List<AuthUI.IdpConfig> providers = new ArrayList<>();
        providers.add(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RC_SIGN_IN);
    }
}
