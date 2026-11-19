package edu.library.service;

/** OBSERVER pattern: anything that wants to react to library changes implements this. */
public interface LibraryListener {
    void onEvent(LibraryEvent event);
}
