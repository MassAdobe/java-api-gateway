package com.guangl.gateway.exception;

import com.guangl.gateway.enums.ErrorCodeMsg;

public class GatewayException extends RuntimeException {
    private ErrorCodeMsg errorCodeMsg;
    private int code;
    private String message;

    public GatewayException(ErrorCodeMsg errorCodeMsg) {
        this.errorCodeMsg = errorCodeMsg;
        this.code = errorCodeMsg.getCode();
        this.message = errorCodeMsg.getMessage();
    }

    public ErrorCodeMsg getErrorCodeMsg() {
        return errorCodeMsg;
    }
}