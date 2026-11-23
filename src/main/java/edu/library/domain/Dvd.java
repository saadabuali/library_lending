package edu.library.domain;

import java.util.Map;

public class Dvd extends LibraryItem {
    private final int durationMinutes;

    public Dvd(String id, String title, int year, int durationMinutes) {
        super(id, title, year);
        this.durationMinutes = Validation.requireRange(durationMinutes, 1, 1000, "Duration");
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public int getLoanPeriodDays() {
        return 7;
    }

    @Override
    public double getDailyLateFee() {
        return 1.00;
    }

    @Override
    public String getTypeName() {
        return "DVD";
    }

    @Override
    public String getDetails() {
        return durationMinutes + " min";
    }

    @Override
    protected void addJsonFields(Map<String, Object> json) {
        json.put("durationMinutes", durationMinutes);
    }
}
