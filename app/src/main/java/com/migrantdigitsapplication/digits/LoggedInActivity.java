package com.migrantdigitsapplication.digits;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import migration.auth.digits.google.com.migrantdigitsapplication.R;

public class LoggedInActivity extends Activity implements View.OnClickListener {
    private TextView mPhoneNumber;
    private TextView mUserId;
    private Button mSignOut;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in_layout);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mPhoneNumber = (TextView) findViewById(R.id.user_phone_number);
        mUserId = (TextView) findViewById(R.id.user_id);
        mSignOut = (Button) findViewById(R.id.sign_out);

        mSignOut.setOnClickListener(this);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null) {
                    startActivity(new Intent(LoggedInActivity.this, MainActivity.class));
                    finish();
                } else {
                    mPhoneNumber.setText(TextUtils.isEmpty(user.getPhoneNumber())
                            ? "No phone" : user.getPhoneNumber());
                    mUserId.setText(TextUtils.isEmpty(user.getUid())
                            ? "No user ID" : user.getUid());
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onClick(View v) {
        AuthUI.getInstance().signOut(LoggedInActivity.this);
    }
}
