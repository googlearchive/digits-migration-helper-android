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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import migration.auth.digits.google.com.digitsmigrationhelpers.AuthMigrator;

public class UpgradeDefaultSessionButtonListener implements Button.OnClickListener {
    private final AuthMigrator authMigrator;

    public UpgradeDefaultSessionButtonListener(AuthMigrator migrator) {
        this.authMigrator = migrator;
    }

    @Override
    public void onClick(View v) {
        authMigrator.migrate().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getUser() != null) {
                        String preservedDigitsPhoneNumber = task.getResult().getUser()
                                .getPhoneNumber();
                        String preservedDigitsId = task.getResult().getUser().getUid();

                        Log.d("Digits", "Preserved Phone Number" + preservedDigitsPhoneNumber);
                        Log.d("Digits", "Preserved User Id" + preservedDigitsId);
                    } else {
                        //No valid digits session was found
                        Log.d("Digits", "No valid legacy digits session found");
                    }
                } else {
                    //an error accured
                    Log.d("Digits", "Error while upgrading digits session: " + task.getException
                            ().getLocalizedMessage());
                }
            }
        });

    }
}
