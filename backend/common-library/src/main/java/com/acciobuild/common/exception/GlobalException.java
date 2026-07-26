package com.acciobuild.common.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final String errorCode;
    private final int status;

    public GlobalException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
