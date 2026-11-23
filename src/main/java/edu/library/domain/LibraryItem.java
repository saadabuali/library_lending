package edu.library.domain;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Abstract parent of everything in the catalogue.
 * Natural ordering is alphabetical by title (Comparable).
 */
public abstract class LibraryItem implements Lendable, Identifiable, JsonSerializable, Comparable<LibraryItem> {
    private final String id;
    private final String title;
    private final int year;
    private boolean available = true;

    protected LibraryItem(String id, String title, int year) {
        this.id = Validation.requireText(id, "Item id");
        this.title = Validation.requireText(title, "Title");
        this.year = Validation.requireRange(year, 1000, 2100, "Year");
    }

    @Override
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public boolean isAvailable() {
        return available;
    }

    /** Called by the library when the item is checked out. */
    public void markBorrowed() {
        available = false;
    }

    /** Called by the library when the item is returned. */
    public void markReturned() {
        available = true;
    }

    /** Type-specific detail shown in listings (author, duration, issue...). */
    public abstract String getDetails();

    /**
     * Template method: writes the fields every item has, then lets the subclass add its own
     * through {@link #addJsonFields(Map)}.
     */
    @Override
    public final Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", getTypeName().toUpperCase(Locale.ROOT));
        json.put("id", id);
        json.put("title", title);
        json.put("year", year);
        addJsonFields(json);
        return json;
    }

    /** Subclass hook: add the type-specific fields (author, duration, issue number...). */
    protected abstract void addJsonFields(Map<String, Object> json);

    @Override
    public int compareTo(LibraryItem other) {
        return title.compareToIgnoreCase(other.title);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-8s %-30s %d  %s  (%s)",
                id, getTypeName(), title, year, getDetails(), available ? "available" : "ON LOAN");
    }
}
