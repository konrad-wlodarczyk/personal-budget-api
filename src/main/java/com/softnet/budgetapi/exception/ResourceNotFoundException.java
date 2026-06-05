package com.softnet.budgetapi.exception;

public class ResourceNotFoundException extends RuntimeException{
    private final ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;

    public ResourceNotFoundException(String message){
        super(message);
    }

    public ErrorCode getErrorCode() {return errorCode;}
}
