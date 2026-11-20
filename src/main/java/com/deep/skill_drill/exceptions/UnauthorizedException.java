package com.deep.skill_drill.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String code, String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }

    public UnauthorizedException(String code, String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, code, message, cause);
    }
}
