package edu.library.service;

import java.time.LocalDate;

/** Immutable event sent to listeners (OBSERVER pattern). */
public record LibraryEvent(Type type, String message, LocalDate date) {
    public enum Type { ITEM_ADDED, MEMBER_REGISTERED, ITEM_BORROWED, ITEM_RETURNED, FEE_POLICY_CHANGED }

    @Override
    public String toString() {
        return date + " " + type + ": " + message;
    }
}
