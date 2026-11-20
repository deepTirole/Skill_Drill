package com.deep.skill_drill.exceptions;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {
    public ExternalServiceException(String code, String message) {
        super(HttpStatus.BAD_GATEWAY, code, message);
    }

    public ExternalServiceException(String code, String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, code, message, cause);
    }
}
