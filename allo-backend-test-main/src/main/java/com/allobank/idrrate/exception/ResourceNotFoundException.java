package com.allobank.idrrate.exception;

/**
 * Exception thrown when an unsupported resource type is requested.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
