package edu.library.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Observer that remembers events so the UI can show recent activity. */
public class ActivityLog implements LibraryListener {
    private final List<LibraryEvent> events = new ArrayList<>();

    @Override
    public void onEvent(LibraryEvent event) {
        events.add(event);
    }

    public List<LibraryEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
