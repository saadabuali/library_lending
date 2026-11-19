package edu.library.service;

import edu.library.domain.LibraryItem;
import edu.library.domain.Loan;
import edu.library.domain.Member;
import edu.library.domain.Registry;
import edu.library.exception.EntityNotFoundException;
import edu.library.exception.ItemUnavailableException;
import edu.library.exception.LibraryException;
import edu.library.exception.LoanLimitExceededException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Main service class. Aggregates the item and member registries, applies the
 * current {@link FeePolicy} (Strategy) and notifies {@link LibraryListener}s (Observer).
 */
public class Library {
    private final Registry<LibraryItem> items = new Registry<>("Item");
    private final Registry<Member> members = new Registry<>("Member");
    private final List<LibraryListener> listeners = new ArrayList<>();
    private FeePolicy feePolicy = new StandardFeePolicy();

    // ---- observers -------------------------------------------------------
    public void addListener(LibraryListener listener) {
        listeners.add(listener);
    }

    private void fire(LibraryEvent.Type type, String message) {
        LibraryEvent event = new LibraryEvent(type, message, LocalDate.now());
        for (LibraryListener l : listeners) {
            l.onEvent(event);
        }
    }

    // ---- strategy --------------------------------------------------------
    public FeePolicy getFeePolicy() {
        return feePolicy;
    }

    public void setFeePolicy(FeePolicy policy) {
        if (policy == null) {
            throw new edu.library.exception.ValidationException("Fee policy is required.");
        }
        this.feePolicy = policy;
        fire(LibraryEvent.Type.FEE_POLICY_CHANGED, "Fee policy is now: " + policy.getName());
    }

    // ---- catalogue and members ------------------------------------------
    public void addItem(LibraryItem item) throws LibraryException {
        if (!items.add(item)) {
            throw new LibraryException("An item with id '" + item.getId() + "' already exists.");
        }
        fire(LibraryEvent.Type.ITEM_ADDED, "Added " + item.getTypeName() + " '" + item.getTitle() + "'.");
    }

    public void registerMember(Member member) throws LibraryException {
        if (!members.add(member)) {
            throw new LibraryException("A member with id '" + member.getId() + "' already exists.");
        }
        fire(LibraryEvent.Type.MEMBER_REGISTERED, "Registered member " + member.getName() + ".");
    }

    /** Next free id such as B001, D002, M003 for the given prefix. */
    public String nextItemId(String prefix) {
        int n = 1;
        while (items.findById(String.format("%s%03d", prefix, n)).isPresent()) {
            n++;
        }
        return String.format("%s%03d", prefix, n);
    }

    public String nextMemberId() {
        int n = 1;
        while (members.findById(String.format("U%03d", n)).isPresent()) {
            n++;
        }
        return String.format("U%03d", n);
    }

    public Registry<LibraryItem> getItems() {
        return items;
    }

    public Registry<Member> getMembers() {
        return members;
    }

    // ---- listing, searching, sorting ------------------------------------
    public List<LibraryItem> itemsSortedByTitle() {
        return items.sorted(Comparator.naturalOrder());
    }

    public List<LibraryItem> itemsSortedByYear() {
        return items.sorted(Comparator.comparingInt(LibraryItem::getYear)
                .thenComparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER));
    }

    public List<LibraryItem> search(String keyword) {
        String k = keyword.toLowerCase(Locale.ROOT).trim();
        return items.findAll(i -> i.getTitle().toLowerCase(Locale.ROOT).contains(k)
                || i.getDetails().toLowerCase(Locale.ROOT).contains(k));
    }

    public List<Loan> activeLoans() {
        List<Loan> result = new ArrayList<>();
        for (Member m : members.all()) {
            result.addAll(m.getActiveLoans());
        }
        result.sort(Comparator.comparing(Loan::getDueDate));
        return result;
    }

    // ---- borrowing and returning ----------------------------------------
    public Loan checkOut(String memberId, String itemId, LocalDate date) throws LibraryException {
        Member member = members.getById(memberId);
        LibraryItem item = items.getById(itemId);
        if (!item.isAvailable()) {
            throw new ItemUnavailableException(item.getId());
        }
        if (!member.canBorrowMore()) {
            throw new LoanLimitExceededException(member.getName(), Member.MAX_LOANS);
        }
        Loan loan = new Loan(member, item, date);
        item.markBorrowed();
        member.addLoan(loan);
        fire(LibraryEvent.Type.ITEM_BORROWED,
                member.getName() + " borrowed '" + item.getTitle() + "', due " + loan.getDueDate() + ".");
        return loan;
    }

    public ReturnReceipt returnItem(String itemId, LocalDate returnDate) throws LibraryException {
        LibraryItem item = items.getById(itemId);
        Loan loan = findLoanFor(item);
        if (loan == null) {
            throw new LibraryException("Item " + item.getId() + " is not on loan.");
        }
        int overdue = loan.daysOverdue(returnDate);
        double fee = feePolicy.calculateFee(item, loan.getMember(), overdue);
        loan.getMember().removeLoan(loan);
        item.markReturned();
        fire(LibraryEvent.Type.ITEM_RETURNED, loan.getMember().getName() + " returned '" + item.getTitle()
                + "'" + (overdue > 0 ? String.format(" (%d days late, fee %.2f)", overdue, fee) : "") + ".");
        return new ReturnReceipt(loan, overdue, fee);
    }

    /** Used by the storage layer to rebuild loans without firing events or checks. */
    public void restoreLoan(String memberId, String itemId, LocalDate loanDate) throws EntityNotFoundException {
        Member member = members.getById(memberId);
        LibraryItem item = items.getById(itemId);
        item.markBorrowed();
        member.addLoan(new Loan(member, item, loanDate));
    }

    private Loan findLoanFor(LibraryItem item) {
        for (Member m : members.all()) {
            for (Loan l : m.getActiveLoans()) {
                if (l.getItem().equals(item)) {
                    return l;
                }
            }
        }
        return null;
    }
}
