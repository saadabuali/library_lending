package edu.library.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A library member. Owns (composition) the list of their active loans. */
public class Member implements Identifiable {
    public static final int MAX_LOANS = 3;

    private final String id;
    private final String name;
    private final MembershipType type;
    private final List<Loan> activeLoans = new ArrayList<>();

    public Member(String id, String name, MembershipType type) {
        this.id = Validation.requireText(id, "Member id");
        this.name = Validation.requireText(name, "Name");
        if (type == null) {
            throw new edu.library.exception.ValidationException("Membership type is required.");
        }
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MembershipType getType() {
        return type;
    }

    /** Read-only view: callers must use addLoan/removeLoan. */
    public List<Loan> getActiveLoans() {
        return Collections.unmodifiableList(activeLoans);
    }

    public boolean canBorrowMore() {
        return activeLoans.size() < MAX_LOANS;
    }

    public void addLoan(Loan loan) {
        activeLoans.add(loan);
    }

    public void removeLoan(Loan loan) {
        activeLoans.remove(loan);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s %-8s loans: %d/%d", id, name, type, activeLoans.size(), MAX_LOANS);
    }
}
