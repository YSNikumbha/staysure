package com.staysure.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message, String errorCode) {
        super(HttpStatus.CONFLICT, message, errorCode);
    }
}
