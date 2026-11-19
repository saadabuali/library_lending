package edu.library.domain;

public class Book extends LibraryItem {
    private final String author;

    public Book(String id, String title, int year, String author) {
        super(id, title, year);
        this.author = Validation.requireText(author, "Author");
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public int getLoanPeriodDays() {
        return 21;
    }

    @Override
    public double getDailyLateFee() {
        return 0.20;
    }

    @Override
    public String getTypeName() {
        return "Book";
    }

    @Override
    public String getDetails() {
        return "by " + author;
    }

    @Override
    public String getExtraField() {
        return author;
    }
}
