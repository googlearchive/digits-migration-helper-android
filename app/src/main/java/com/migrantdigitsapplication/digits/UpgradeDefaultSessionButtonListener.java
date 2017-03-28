package com.migrantdigitsapplication.digits;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import migration.auth.digits.google.com.digitsmigrationhelpers.AuthMigrator;

public class UpgradeDefaultSessionButtonListener implements Button.OnClickListener {
    private final Context context;
    private final AuthMigrator authMigrator;

    public UpgradeDefaultSessionButtonListener(Context context, AuthMigrator migrator) {
        this.context = context;
        this.authMigrator = migrator;
    }

    @Override
    public void onClick(View v) {
        authMigrator.migrate().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                                "No valid digits session found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    //an error accured
                    Toast.makeText(context,
                            "Error while upgrading digits session: "
                                    + task.getException().getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
