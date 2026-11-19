package edu.library.service;

import edu.library.domain.LibraryItem;
import edu.library.domain.Member;

/** Everyone pays the item's daily rate for each overdue day. */
public class StandardFeePolicy implements FeePolicy {
    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public double calculateFee(LibraryItem item, Member member, int daysOverdue) {
        return item.getDailyLateFee() * daysOverdue;
    }
}
