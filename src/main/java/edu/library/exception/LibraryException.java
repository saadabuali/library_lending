package edu.library.exception;

/** Base checked exception for all domain-rule violations in the library. */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
