package com.migrantdigitsapplication.digits;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import migration.auth.digits.google.com.migrantdigitsapplication.R;

public class MainActivity extends AppCompatActivity {
    private Button setDigitsSessionButton;
    private EditText digitsAuthTokenEditText;
    private EditText digitsAuthTokenSecretEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDigitsSessionButton = (Button) findViewById(R.id.set_digits_session_button);
        digitsAuthTokenEditText = (EditText) findViewById(R.id.digits_auth_token_edit_text);
        digitsAuthTokenSecretEditText = (EditText) findViewById(R.id.digits_auth_token_secret_edit_text);

        setDigitsSessionButton.setOnClickListener(
                new SetDigitsSessionButtonListener(
                        this,
                        digitsAuthTokenEditText,
                        digitsAuthTokenSecretEditText,
                        new SessionOverwriteAlertDialog(this)
                )
        );
    }
}
