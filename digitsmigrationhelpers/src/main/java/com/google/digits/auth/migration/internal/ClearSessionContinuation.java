/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.digits.auth.migration.internal;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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