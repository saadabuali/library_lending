package edu.library.domain;

/**
 * Abstract parent of everything in the catalogue.
 * Natural ordering is alphabetical by title (Comparable).
 */
public abstract class LibraryItem implements Lendable, Identifiable, Comparable<LibraryItem> {
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

    /** Type-specific value written to the data file. */
    public abstract String getExtraField();

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
