package edu.library.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/** One member borrowing one item. Immutable once created. */
public class Loan implements JsonSerializable {
    private final Member member;
    private final LibraryItem item;
    private final LocalDate loanDate;
    private final LocalDate dueDate;

    public Loan(Member member, LibraryItem item, LocalDate loanDate) {
        if (member == null || item == null || loanDate == null) {
            throw new edu.library.exception.ValidationException("Loan needs a member, an item and a date.");
        }
        this.member = member;
        this.item = item;
        this.loanDate = loanDate;
        this.dueDate = loanDate.plusDays(item.getLoanPeriodDays());
    }

    public Member getMember() {
        return member;
    }

    public LibraryItem getItem() {
        return item;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    /** Days past the due date when returned on {@code returnDate} (0 if on time). */
    public int daysOverdue(LocalDate returnDate) {
        long days = ChronoUnit.DAYS.between(dueDate, returnDate);
        return (int) Math.max(0, days);
    }

    /** Only the references and the loan date are stored; the due date is recalculated on load. */
    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("memberId", member.getId());
        json.put("itemId", item.getId());
        json.put("loanDate", loanDate.toString());
        return json;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (borrowed %s, due %s)",
                item.getTitle(), member.getName(), loanDate, dueDate);
    }
}
