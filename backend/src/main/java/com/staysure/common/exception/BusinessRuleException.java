package com.staysure.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message, String errorCode) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }

    public BusinessRuleException(String message, String errorCode, Object data) {
        super(HttpStatus.BAD_REQUEST, message, errorCode, data);
    }
}
