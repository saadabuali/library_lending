package edu.library.service;

import edu.library.domain.Loan;

/** Result of returning an item. */
public record ReturnReceipt(Loan loan, int daysOverdue, double fee) {
}
