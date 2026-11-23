package edu.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.library.domain.Book;
import edu.library.domain.Dvd;
import edu.library.domain.JsonSerializable;
import edu.library.domain.LibraryItem;
import edu.library.domain.Magazine;
import edu.library.domain.Member;
import edu.library.domain.MembershipType;
import edu.library.exception.ItemUnavailableException;
import edu.library.exception.LibraryException;
import edu.library.exception.LoanLimitExceededException;
import edu.library.exception.StorageException;
import edu.library.exception.ValidationException;
import edu.library.persistence.JsonFileStorage;
import edu.library.service.AmnestyFeePolicy;
import edu.library.service.Library;
import edu.library.service.LibraryEvent;
import edu.library.service.ReturnReceipt;
import edu.library.service.StudentDiscountFeePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LibraryTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Library library;
    private final LocalDate day = LocalDate.of(2026, 11, 1);

    @Before
    public void setUp() throws LibraryException {
        library = new Library();
        library.addItem(new Book("B001", "Clean Code", 2008, "Robert Martin"));
        library.addItem(new Dvd("D001", "The Matrix", 1999, 136));
        library.addItem(new Magazine("M001", "Java Magazine", 2024, 12));
        library.registerMember(new Member("U001", "Anna", MembershipType.STANDARD));
        library.registerMember(new Member("U002", "Peter", MembershipType.STUDENT));
    }

    // ---- encapsulation / validation
    @Test(expected = ValidationException.class)
    public void blankTitleIsRejected() {
        new Book("B9", "  ", 2000, "Someone");
    }

    @Test(expected = ValidationException.class)
    public void impossibleYearIsRejected() {
        new Dvd("D9", "Film", 5, 100);
    }

    // ---- polymorphism
    @Test
    public void itemTypesBehaveDifferently() {
        List<LibraryItem> items = library.getItems().all();
        assertEquals(21, items.get(0).getLoanPeriodDays());
        assertEquals(7, items.get(1).getLoanPeriodDays());
        assertEquals(1.00, items.get(1).getDailyLateFee(), 0.0001);
    }

    // ---- borrowing rules
    @Test
    public void checkOutMarksItemUnavailable() throws LibraryException {
        library.checkOut("U001", "B001", day);
        assertFalse(library.getItems().getById("B001").isAvailable());
        assertEquals(1, library.getMembers().getById("U001").getActiveLoans().size());
    }

    @Test(expected = ItemUnavailableException.class)
    public void cannotBorrowSameItemTwice() throws LibraryException {
        library.checkOut("U001", "B001", day);
        library.checkOut("U002", "B001", day);
    }

    @Test
    public void loanLimitIsEnforced() throws LibraryException {
        library.addItem(new Book("B002", "Book 2", 2001, "A"));
        library.addItem(new Book("B003", "Book 3", 2002, "B"));
        library.addItem(new Book("B004", "Book 4", 2003, "C"));
        library.checkOut("U001", "B001", day);
        library.checkOut("U001", "B002", day);
        library.checkOut("U001", "B003", day);
        try {
            library.checkOut("U001", "B004", day);
            fail("expected LoanLimitExceededException");
        } catch (LoanLimitExceededException expected) {
            assertTrue(expected.getMessage().contains("limit"));
        }
    }

    // ---- strategy
    @Test
    public void lateFeeDependsOnPolicy() throws LibraryException {
        library.checkOut("U002", "D001", day);              // DVD, due day+7
        LocalDate returned = day.plusDays(10);               // 3 days late
        ReturnReceipt standard = library.returnItem("D001", returned);
        assertEquals(3, standard.daysOverdue());
        assertEquals(3.00, standard.fee(), 0.0001);

        library.setFeePolicy(new StudentDiscountFeePolicy());
        library.checkOut("U002", "D001", day);
        assertEquals(1.50, library.returnItem("D001", returned).fee(), 0.0001);

        library.setFeePolicy(new AmnestyFeePolicy());
        library.checkOut("U002", "D001", day);
        assertEquals(0.0, library.returnItem("D001", returned).fee(), 0.0001);
    }

    @Test
    public void onTimeReturnHasNoFee() throws LibraryException {
        library.checkOut("U001", "B001", day);
        assertEquals(0.0, library.returnItem("B001", day.plusDays(21)).fee(), 0.0001);
    }

    // ---- observer
    @Test
    public void listenersAreNotified() throws LibraryException {
        List<LibraryEvent> seen = new ArrayList<>();
        library.addListener(seen::add);
        library.checkOut("U001", "B001", day);
        library.returnItem("B001", day);
        assertEquals(2, seen.size());
        assertEquals(LibraryEvent.Type.ITEM_BORROWED, seen.get(0).type());
        assertEquals(LibraryEvent.Type.ITEM_RETURNED, seen.get(1).type());
    }

    // ---- sorting / searching
    @Test
    public void sortingByTitleAndYear() {
        assertEquals("Clean Code", library.itemsSortedByTitle().get(0).getTitle());
        assertEquals("The Matrix", library.itemsSortedByYear().get(0).getTitle());
    }

    @Test
    public void searchFindsByTitleAndAuthor() {
        assertEquals(1, library.search("matrix").size());
        assertEquals(1, library.search("martin").size());
        assertTrue(library.search("zzz").isEmpty());
    }

    // ---- persistence
    @Test
    public void saveAndLoadRoundTrip() throws LibraryException, StorageException {
        library.checkOut("U001", "B001", day);
        Path file = tmp.getRoot().toPath().resolve("data").resolve("lib.json");
        JsonFileStorage storage = new JsonFileStorage(file);
        storage.save(library);

        Library loaded = storage.load();
        assertEquals(3, loaded.getItems().size());
        assertEquals(2, loaded.getMembers().size());
        assertFalse(loaded.getItems().getById("B001").isAvailable());
        assertEquals(day.plusDays(21), loaded.activeLoans().get(0).getDueDate());
    }

    @Test
    public void missingFileGivesEmptyLibrary() throws StorageException {
        Library loaded = new JsonFileStorage(tmp.getRoot().toPath().resolve("nope.json")).load();
        assertEquals(0, loaded.getItems().size());
    }

    @Test
    public void corruptFileIsReported() throws IOException {
        Path file = tmp.newFile("bad.json").toPath();
        Files.writeString(file, "{\"items\": [{\"type\": \"BOOK\", \"id\": \"B1\", \"title\": \"Title\","
                + " \"year\": \"notAYear\", \"author\": \"Author\"}]}");
        try {
            new JsonFileStorage(file).load();
            fail("expected StorageException");
        } catch (StorageException expected) {
            assertTrue(expected.getMessage().contains("item #1"));
        }
    }

    @Test
    public void malformedJsonIsReported() throws IOException {
        Path file = tmp.newFile("broken.json").toPath();
        Files.writeString(file, "{\"items\": [");
        try {
            new JsonFileStorage(file).load();
            fail("expected StorageException");
        } catch (StorageException expected) {
            assertTrue(expected.getMessage().contains("Invalid JSON"));
        }
    }

    @Test
    public void specialCharactersSurviveRoundTrip() throws LibraryException, StorageException {
        Library lib = new Library();
        lib.addItem(new Book("B9", "He said \"hi\" | back\\slash \u00e9\u00fc", 2020, "A\tB"));
        Path file = tmp.getRoot().toPath().resolve("special.json");
        JsonFileStorage storage = new JsonFileStorage(file);
        storage.save(lib);
        assertEquals("He said \"hi\" | back\\slash \u00e9\u00fc", storage.load().getItems().getById("B9").getTitle());
    }

    // ---- object-oriented JSON: every object writes itself
    @Test
    public void itemsDescribeThemselvesPolymorphically() throws LibraryException {
        List<JsonSerializable> things = new ArrayList<>(library.getItems().all());
        Map<String, Object> book = things.get(0).toJson();
        Map<String, Object> dvd = things.get(1).toJson();
        Map<String, Object> magazine = things.get(2).toJson();
        assertEquals("BOOK", book.get("type"));
        assertEquals("Robert Martin", book.get("author"));
        assertEquals("DVD", dvd.get("type"));
        assertEquals(136, dvd.get("durationMinutes"));
        assertFalse(dvd.containsKey("author"));
        assertEquals("MAGAZINE", magazine.get("type"));
        assertEquals(12, magazine.get("issueNumber"));
    }

    @Test
    public void memberAndLoanDescribeThemselves() throws LibraryException {
        library.checkOut("U002", "D001", day);
        Map<String, Object> member = library.getMembers().getById("U002").toJson();
        assertEquals("STUDENT", member.get("type"));
        Map<String, Object> loan = library.activeLoans().get(0).toJson();
        assertEquals("U002", loan.get("memberId"));
        assertEquals("D001", loan.get("itemId"));
        assertEquals("2026-11-01", loan.get("loanDate"));
    }
}
