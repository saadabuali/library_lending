package edu.library.domain;

public class Magazine extends LibraryItem {
    private final int issueNumber;

    public Magazine(String id, String title, int year, int issueNumber) {
        super(id, title, year);
        this.issueNumber = Validation.requireRange(issueNumber, 1, 100000, "Issue number");
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public int getLoanPeriodDays() {
        return 7;
    }

    @Override
    public double getDailyLateFee() {
        return 0.10;
    }

    @Override
    public String getTypeName() {
        return "Magazine";
    }

    @Override
    public String getDetails() {
        return "issue #" + issueNumber;
    }

    @Override
    public String getExtraField() {
        return String.valueOf(issueNumber);
    }
}
