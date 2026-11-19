package edu.library.service;

import edu.library.domain.LibraryItem;
import edu.library.domain.Member;

/** STRATEGY pattern: the late-fee rule can be swapped at runtime. */
public interface FeePolicy {
    String getName();

    double calculateFee(LibraryItem item, Member member, int daysOverdue);
}
