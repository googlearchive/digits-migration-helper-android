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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import migration.auth.digits.google.com.migrantdigitsapplication.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 123;
    private ProgressDialog mProgressDialog;
    private Button signinInButton;
    private Task<Void> migratorTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_layout);

        signinInButton = (Button) findViewById(R.id.sign_in);
        signinInButton.setOnClickListener(this);
        migratorTask = MigrantDigitsApplication.get(this).getDigitsMigratorTask();
        mProgressDialog = new ProgressDialog(this);

        if(!migratorTask.isComplete()) {
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle(getString(R.string.signing_in));
            mProgressDialog.setMessage(getString(R.string.looking_for_digits_session));
            mProgressDialog.show();
        }

        migratorTask.addOnSuccessListener(this,
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void authResult) {
                        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();

                        if (u != null) {
                            // Either a user was already logged in or token exchange succeeded
                            Log.d("MyApp", "Digits id preserved:" + u.getUid());
                            Log.d("MyApp", "Digits phone number preserved: " + u.getPhoneNumber());
                            mProgressDialog.setMessage("Logged in!");
                            mProgressDialog.dismiss();
                            startLoggedInUX();
                        } else {
                            // No tokens were found to exchange and no firebase user logged in.
                            mProgressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(this,
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error migrating digits token
                        mProgressDialog.dismiss();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == RC_SIGN_IN) {
            Toast.makeText(this, R.string.signed_in_sing_firebase_ui, Toast.LENGTH_LONG).show();
            startLoggedInUX();
        }
    }

    @Override
    public void onClick(View v) {
        startLoginWithFirebaseUI();
    }

    private void startLoggedInUX() {
        startActivity(new Intent(MainActivity.this, LoggedInActivity.class));
        finish();
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
