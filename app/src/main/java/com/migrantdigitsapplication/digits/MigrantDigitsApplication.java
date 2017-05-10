package com.migrantdigitsapplication.digits;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import migration.auth.digits.google.com.digitsmigrationhelpers.AuthMigrator;
import migration.auth.digits.google.com.migrantdigitsapplication.BuildConfig;

public class MigrantDigitsApplication extends Application {
    private Task<AuthResult> digitsMigratorTask;

    public static MigrantDigitsApplication get(Context context) {
        return (MigrantDigitsApplication) context.getApplicationContext();
    }

    public Task<AuthResult> getDigitsMigratorTask() {
        return digitsMigratorTask;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Does not block calling thread
        digitsMigratorTask = AuthMigrator.getInstance().migrate(!BuildConfig.DEBUG);
    }
}