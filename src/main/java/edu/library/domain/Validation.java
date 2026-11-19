package edu.library.domain;

import edu.library.exception.ValidationException;

/** Small helper so every constructor validates the same way. */
final class Validation {
    private Validation() { }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " must not be empty.");
        }
        return value.trim();
    }

    static int requireRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }
}
