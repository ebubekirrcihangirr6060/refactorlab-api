package com.refactorlab.api.rest;

// Geçersiz istek (ör. PATCH gövdesinde id, çok uzun kod) -> 400
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
