package com.google.digits.auth.migration.internal;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

/**
 * @hide
 */
public class ClearSessionContinuation implements Continuation<AuthResult, Task<AuthResult>> {

    private final StorageHelpers storageHelpers;

    public ClearSessionContinuation(StorageHelpers storageHelpers) {
        this.storageHelpers = storageHelpers;
    }

    @Override
    public Task<AuthResult> then(@NonNull Task<AuthResult> task) throws Exception {
        if (!task.isSuccessful()) {
            try {
                throw task.getException();
            } catch (FirebaseWebRequestException e) {
                if (e.getHttpStatusCode() == 400 || e.getHttpStatusCode() == 403) {
                    // Permanent errors should clear the persistence key.
                    storageHelpers.clearDigitsSession();
                }
                return task;
            }
        }
        storageHelpers.clearDigitsSession();
        return task;
    }
}