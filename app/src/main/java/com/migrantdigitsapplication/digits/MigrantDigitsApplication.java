package com.migrantdigitsapplication.digits;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.tasks.Task;

import com.firebase.digitsmigrationhelpers.AuthMigrator;
import migration.auth.digits.google.com.migrantdigitsapplication.BuildConfig;

public class MigrantDigitsApplication extends Application {
    private Task<Void> digitsMigratorTask;

    @Override
    public void onCreate() {
        super.onCreate();
        // Does not block calling thread
        // Kick off migration asap.
        digitsMigratorTask = AuthMigrator.getInstance().migrate(!BuildConfig.DEBUG);
    }

    public static MigrantDigitsApplication get(Context context) {
        return (MigrantDigitsApplication) context.getApplicationContext();
    }

    public Task<Void> getDigitsMigratorTask() {
        return digitsMigratorTask;
    }
}