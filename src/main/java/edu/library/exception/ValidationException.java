package edu.library.exception;

/** Unchecked exception for invalid input values (blank title, bad year, ...). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
