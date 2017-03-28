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