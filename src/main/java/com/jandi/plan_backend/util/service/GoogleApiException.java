package com.jandi.plan_backend.util.service;

public class GoogleApiException extends RuntimeException {

    public GoogleApiException(Throwable cause) {
        super("Error occurred while calling Google Places API: " + cause.getMessage(), cause);
    }

    public GoogleApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
