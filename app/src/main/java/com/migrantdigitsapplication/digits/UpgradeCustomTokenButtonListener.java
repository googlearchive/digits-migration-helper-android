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

package com.migrantdigitsapplication.digits;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import migration.auth.digits.google.com.digitsmigrationhelpers.AuthMigrator;
import migration.auth.digits.google.com.digitsmigrationhelpers.RedeemableDigitsSessionBuilder;

class UpgradeCustomTokenButtonListener implements Button.OnClickListener {
    private final AuthMigrator authMigrator;
    final Context context;
    final EditText tokenEditText;
    final EditText secretEditText;

    public UpgradeCustomTokenButtonListener(
            Context context,
            AuthMigrator migrator,
            EditText tokenEditText,
            EditText secretEditText
    ) {
        this.context = context;
        this.authMigrator = migrator;
        this.tokenEditText = tokenEditText;
        this.secretEditText = secretEditText;
    }

    @Override
    public void onClick(View v) {
        final String token = tokenEditText.getText().toString();
        final String secret = secretEditText.getText().toString();
        RedeemableDigitsSessionBuilder builder = new RedeemableDigitsSessionBuilder()
                .setAuthToken(token)
                .setAuthTokenSecret(secret);
        authMigrator.migrate(builder).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getUser() != null) {
                        Toast.makeText(context,
                                "Found user " + task.getResult().getUser().getUid(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        //No valid digits session was found
                        Toast.makeText(context,
                                "No valid digits session found",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    //an error accured
                    Toast.makeText(context,
                            "Error while upgrading digits session",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}