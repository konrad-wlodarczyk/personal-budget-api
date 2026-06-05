package com.softnet.budgetapi.exception;

public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode = ErrorCode.BUSINESS_RULE_CONFLICT;

    public BusinessException(String message) {
        super(message);
    }

    public ErrorCode getErrorCode(){return errorCode;}
}
