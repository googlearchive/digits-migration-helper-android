package com.google.digits.auth.migration.internal;

import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class MigratorAuthResult implements AuthResult {
    private final FirebaseUser user;

    public MigratorAuthResult(FirebaseUser user) {
        this.user = user;
    }

    @Override
    public FirebaseUser getUser() {
        return user;
    }

    @Override
    public AdditionalUserInfo getAdditionalUserInfo() {
        return null;
    }
}