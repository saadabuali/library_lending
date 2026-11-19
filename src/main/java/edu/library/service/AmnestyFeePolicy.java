package edu.library.service;

import edu.library.domain.LibraryItem;
import edu.library.domain.Member;

/** Amnesty week: no late fees at all. */
public class AmnestyFeePolicy implements FeePolicy {
    @Override
    public String getName() {
        return "Amnesty (no fees)";
    }

    @Override
    public double calculateFee(LibraryItem item, Member member, int daysOverdue) {
        return 0.0;
    }
}
