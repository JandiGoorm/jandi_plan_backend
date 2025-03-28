package com.jandi.plan_backend.util.service;

/**에러 메시지 폼 util*/
public class BadRequestExceptionMessage extends RuntimeException {

    public BadRequestExceptionMessage(String message) {
        super(message);
    }
}
