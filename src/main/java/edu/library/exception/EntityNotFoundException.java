package edu.library.exception;

/** Thrown when an item or member id does not exist. */
public class EntityNotFoundException extends LibraryException {
    public EntityNotFoundException(String kind, String id) {
        super(kind + " with id '" + id + "' was not found.");
    }
}
