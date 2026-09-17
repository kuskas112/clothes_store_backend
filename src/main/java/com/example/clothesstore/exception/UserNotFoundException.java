package com.example.clothesstore.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("User " + id + " not found");
    }
}