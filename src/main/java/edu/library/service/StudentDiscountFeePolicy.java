package edu.library.service;

import edu.library.domain.LibraryItem;
import edu.library.domain.Member;
import edu.library.domain.MembershipType;

/** Students pay half; other members pay the full standard fee. */
public class StudentDiscountFeePolicy implements FeePolicy {
    @Override
    public String getName() {
        return "Student discount (50% off for students)";
    }

    @Override
    public double calculateFee(LibraryItem item, Member member, int daysOverdue) {
        double fee = item.getDailyLateFee() * daysOverdue;
        return member.getType() == MembershipType.STUDENT ? fee / 2 : fee;
    }
}
