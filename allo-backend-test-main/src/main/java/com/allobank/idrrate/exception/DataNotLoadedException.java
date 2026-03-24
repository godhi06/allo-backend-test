package com.allobank.idrrate.exception;

/**
 * Exception thrown when data has not been loaded into the in-memory store yet.
 */
public class DataNotLoadedException extends RuntimeException {

    public DataNotLoadedException(String message) {
        super(message);
    }
}
