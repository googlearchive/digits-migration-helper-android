package com.migrantdigitsapplication.digits;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import migration.auth.digits.google.com.migrantdigitsapplication.R;

public class LoggedInActivity extends Activity {
    TextView mPhoneNumber;
    TextView mUserId;
    Button mSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in_layout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mPhoneNumber = (TextView) findViewById(R.id.user_phone_number);
        mUserId = (TextView) findViewById(R.id.user_id);
        mSignOut = (Button) findViewById(R.id.sign_out);

        mPhoneNumber.setText(TextUtils.isEmpty(user.getPhoneNumber()) ? "No phone"
                : user.getPhoneNumber());
        mUserId.setText(TextUtils.isEmpty(user.getUid()) ? "No user ID" : user.getUid());
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(LoggedInActivity.this).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LoggedInActivity.this,
                                "You have signed out. Restart the app to test behavior",
                                Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }
}
