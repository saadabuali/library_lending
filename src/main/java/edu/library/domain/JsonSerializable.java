package edu.library.domain;

import java.util.Map;

/**
 * Contract for objects that can describe themselves as JSON data.
 * Each class decides which fields it writes (polymorphism), so the storage layer
 * never needs {@code instanceof} checks or knowledge of the concrete types.
 */
public interface JsonSerializable {
    /** @return the object's JSON fields (values: String, Number, Boolean, List or Map), in write order */
    Map<String, Object> toJson();
}
