package edu.library.exception;

/** Thrown when someone tries to borrow an item that is already on loan. */
public class ItemUnavailableException extends LibraryException {
    public ItemUnavailableException(String itemId) {
        super("Item " + itemId + " is currently on loan.");
    }
}
