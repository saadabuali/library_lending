package edu.library.ui;

import edu.library.domain.Book;
import edu.library.domain.Dvd;
import edu.library.domain.LibraryItem;
import edu.library.domain.Loan;
import edu.library.domain.Magazine;
import edu.library.domain.Member;
import edu.library.domain.MembershipType;
import edu.library.exception.LibraryException;
import edu.library.exception.StorageException;
import edu.library.exception.ValidationException;
import edu.library.persistence.LibraryStorage;
import edu.library.service.ActivityLog;
import edu.library.service.AmnestyFeePolicy;
import edu.library.service.ConsoleNotifier;
import edu.library.service.FeePolicy;
import edu.library.service.Library;
import edu.library.service.ReturnReceipt;
import edu.library.service.StandardFeePolicy;
import edu.library.service.StudentDiscountFeePolicy;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Text menu: reads input, calls the {@link Library} service, prints results. */
public class ConsoleUI {
    private final Library library;
    private final LibraryStorage storage;
    private final ActivityLog activityLog = new ActivityLog();
    private final Scanner in;

    public ConsoleUI(Library library, LibraryStorage storage, Scanner in) {
        this.library = library;
        this.storage = storage;
        this.in = in;
        library.addListener(new ConsoleNotifier());
        library.addListener(activityLog);
    }

    public void run() {
        System.out.println("=== Library Lending System ===");
        boolean running = true;
        while (running) {
            printMenu();
            try {
                String choice = readLine("Choose: ");
                switch (choice) {
                    case "1" -> printItems(library.itemsSortedByTitle());
                    case "2" -> printItems(library.itemsSortedByYear());
                    case "3" -> search();
                    case "4" -> addItem();
                    case "5" -> registerMember();
                    case "6" -> library.getMembers().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                            .forEach(System.out::println);
                    case "7" -> checkOut();
                    case "8" -> returnItem();
                    case "9" -> printLoans();
                    case "10" -> changeFeePolicy();
                    case "11" -> activityLog.getEvents().forEach(System.out::println);
                    case "12" -> loadSampleData();
                    case "0" -> {
                        save();
                        running = false;
                    }
                    default -> System.out.println("Unknown option.");
                }
            } catch (LibraryException | ValidationException e) {
                System.out.println("  Error: " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println("\nInput closed, saving and exiting.");
                save();
                running = false;
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("""

                 1) List items (by title)      7) Check out an item
                 2) List items (by year)       8) Return an item
                 3) Search items               9) Show active loans
                 4) Add item                  10) Change late-fee policy
                 5) Register member           11) Recent activity
                 6) List members              12) Load sample data
                 0) Save and exit""");
    }

    // ---- actions ---------------------------------------------------------
    private void search() {
        printItems(library.search(readLine("Keyword: ")));
    }

    private void addItem() throws LibraryException {
        String kind = readLine("Type (1=Book, 2=DVD, 3=Magazine): ");
        String title = readLine("Title: ");
        int year = readInt("Year: ");
        LibraryItem item;
        switch (kind) {
            case "1" -> item = new Book(library.nextItemId("B"), title, year, readLine("Author: "));
            case "2" -> item = new Dvd(library.nextItemId("D"), title, year, readInt("Duration (minutes): "));
            case "3" -> item = new Magazine(library.nextItemId("M"), title, year, readInt("Issue number: "));
            default -> {
                System.out.println("Unknown type.");
                return;
            }
        }
        library.addItem(item);
        System.out.println("  Created " + item.getId());
        save();
    }

    private void registerMember() throws LibraryException {
        String name = readLine("Name: ");
        String t = readLine("Membership (1=Standard, 2=Student): ");
        MembershipType type = t.equals("2") ? MembershipType.STUDENT : MembershipType.STANDARD;
        Member m = new Member(library.nextMemberId(), name, type);
        library.registerMember(m);
        System.out.println("  Created " + m.getId());
        save();
    }

    private void checkOut() throws LibraryException {
        String memberId = readLine("Member id: ");
        String itemId = readLine("Item id: ");
        Loan loan = library.checkOut(memberId, itemId, readDate("Date (yyyy-MM-dd, blank = today): "));
        System.out.println("  Due back on " + loan.getDueDate());
        save();
    }

    private void returnItem() throws LibraryException {
        String itemId = readLine("Item id: ");
        LocalDate date = readDate("Return date (yyyy-MM-dd, blank = today): ");
        ReturnReceipt r = library.returnItem(itemId, date);
        System.out.printf("  Days overdue: %d, fee (%s): %.2f%n", r.daysOverdue(),
                library.getFeePolicy().getName(), r.fee());
        save();
    }

    private void printLoans() {
        List<Loan> loans = library.activeLoans();
        if (loans.isEmpty()) {
            System.out.println("  No active loans.");
        }
        loans.forEach(System.out::println);
    }

    private void changeFeePolicy() {
        System.out.println("Current policy: " + library.getFeePolicy().getName());
        String c = readLine("1) Standard  2) Student discount  3) Amnesty : ");
        FeePolicy policy = switch (c) {
            case "1" -> new StandardFeePolicy();
            case "2" -> new StudentDiscountFeePolicy();
            case "3" -> new AmnestyFeePolicy();
            default -> null;
        };
        if (policy == null) {
            System.out.println("Unknown option.");
        } else {
            library.setFeePolicy(policy);
        }
    }

    private void loadSampleData() throws LibraryException {
        library.addItem(new Book(library.nextItemId("B"), "Clean Code", 2008, "Robert C. Martin"));
        library.addItem(new Book(library.nextItemId("B"), "Effective Java", 2018, "Joshua Bloch"));
        library.addItem(new Dvd(library.nextItemId("D"), "The Matrix", 1999, 136));
        library.addItem(new Magazine(library.nextItemId("M"), "Java Magazine", 2024, 12));
        library.registerMember(new Member(library.nextMemberId(), "Anna Kovacs", MembershipType.STANDARD));
        library.registerMember(new Member(library.nextMemberId(), "Peter Nagy", MembershipType.STUDENT));
        save();
    }

    // ---- helpers ---------------------------------------------------------
    private void printItems(List<LibraryItem> items) {
        if (items.isEmpty()) {
            System.out.println("  (nothing found)");
        }
        items.forEach(System.out::println);
    }

    private void save() {
        try {
            storage.save(library);
        } catch (StorageException e) {
            System.out.println("  Warning: " + e.getMessage());
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int readInt(String prompt) {
        String text = readLine(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' is not a whole number.");
        }
    }

    private LocalDate readDate(String prompt) {
        String text = readLine(prompt);
        if (text.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("'" + text + "' is not a valid date (use yyyy-MM-dd).");
        }
    }
}
