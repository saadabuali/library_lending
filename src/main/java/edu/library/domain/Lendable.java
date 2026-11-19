package edu.library.domain;

/**
 * Stable contract for anything the library can lend out.
 * (Abstraction: clients depend on this, not on concrete item types.)
 */
public interface Lendable {
    /** Number of days the item may be kept before it is overdue. */
    int getLoanPeriodDays();

    /** Base late fee per overdue day, before any fee policy is applied. */
    double getDailyLateFee();

    /** Human readable type name, e.g. "Book". */
    String getTypeName();
}
