package com.google.digits.auth.migration.internal;

import com.google.firebase.FirebaseException;

/**
 * @hide
 */
public class FirebaseWebRequestException extends FirebaseException {
    private final int httpStatusCode;

    public FirebaseWebRequestException(String message, int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}