package com.migrantdigitsapplication.digits;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

class SetDigitsSessionButtonListener implements Button.OnClickListener {
    final Context context;
    final AlertDialog.Builder alertDialogBuilder;
    final EditText tokenEditText;
    final EditText secretEditText;

    static final String SESSION_PREF_FILE_NAME = "com.digits.sdk.android:digits:session_store";
    static final String PREF_KEY_ACTIVE_SESSION = "active_session";
    final String JSON_SERIALIZED_DIGITS_SESSION_FORMAT =
            "{\"auth_token\":{\"auth_type\":\"oauth1a\"," +
                    "\"auth_token\":{\"secret\":\"%s\",\"token\":\"%s\",\"created_at\":0}}}";

    SetDigitsSessionButtonListener(Context context, EditText tokenEditText, EditText secretEditText, AlertDialog.Builder alertDialogBuilder) {
        this.context = context;
        this.alertDialogBuilder = alertDialogBuilder;
        this.tokenEditText = tokenEditText;
        this.secretEditText = secretEditText;
    }

    @Override
    public void onClick(View v) {
        final SharedPreferences prefs = context.getSharedPreferences(
                SESSION_PREF_FILE_NAME,
                MODE_PRIVATE
        );
        final String token = tokenEditText.getText().toString();
        final String secret = secretEditText.getText().toString();

        if(TextUtils.isEmpty(token) || TextUtils.isEmpty(secret)) {
            Toast.makeText(context, "Auth Token / Token Secret cannot be empty!", Toast.LENGTH_LONG).show();
        } else if(TextUtils.isEmpty(prefs.getString(PREF_KEY_ACTIVE_SESSION, ""))) {
            writeSession(prefs, token, secret);
        } else {
            alertDialogBuilder
                    .setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                writeSession(prefs, token, secret);

                            }
                        });
            alertDialogBuilder.create().show();
        }
    }

    private void writeSession(SharedPreferences prefs, String token, String secret) {
        prefs.edit().putString(
                PREF_KEY_ACTIVE_SESSION,
                String.format(
                        JSON_SERIALIZED_DIGITS_SESSION_FORMAT,
                        secret,
                        token
                )
        ).apply();
    }
}