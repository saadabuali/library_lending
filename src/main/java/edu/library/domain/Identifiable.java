package edu.library.domain;

/** Anything stored in a {@link Registry} must expose a unique id. */
public interface Identifiable {
    String getId();
}
