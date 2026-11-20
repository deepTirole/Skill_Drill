package com.deep.skill_drill.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {
    public BadRequestException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }

    public BadRequestException(String code, String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, code, message, cause);
    }
}
