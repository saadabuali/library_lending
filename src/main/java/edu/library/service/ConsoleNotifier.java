package edu.library.service;

/** Observer that prints a short notice for every event. */
public class ConsoleNotifier implements LibraryListener {
    @Override
    public void onEvent(LibraryEvent event) {
        System.out.println("  [notice] " + event.message());
    }
}
