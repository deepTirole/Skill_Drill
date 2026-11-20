package com.deep.skill_drill.exceptions;

import org.springframework.http.HttpStatus;

public class GoneException extends AppException {
    public GoneException(String code, String message) {
        super(HttpStatus.GONE, code, message);
    }
}
