package com.example.clothesstore.exception;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException() {
        super("User is not authorized");
    }
}