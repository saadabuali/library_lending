package edu.library.persistence;

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
import edu.library.service.Library;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Saves the library as a JSON file:
 * <pre>
 * {
 *   "items":   [ {"type": "BOOK", "id": "B001", "title": "...", "year": 2008, "author": "..."} ],
 *   "members": [ {"id": "U001", "name": "...", "type": "STANDARD"} ],
 *   "loans":   [ {"memberId": "U001", "itemId": "B001", "loanDate": "2026-11-01"} ]
 * }
 * </pre>
 * DVDs use {@code "durationMinutes"} and magazines use {@code "issueNumber"} instead of {@code "author"}.
 */
public class JsonFileStorage implements LibraryStorage {
    private final Path file;

    public JsonFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public void save(Library library) throws StorageException {
        List<Object> items = new ArrayList<>();
        for (LibraryItem i : library.getItems().all()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("type", i.getTypeName().toUpperCase());
            o.put("id", i.getId());
            o.put("title", i.getTitle());
            o.put("year", i.getYear());
            if (i instanceof Book b) {
                o.put("author", b.getAuthor());
            } else if (i instanceof Dvd d) {
                o.put("durationMinutes", d.getDurationMinutes());
            } else if (i instanceof Magazine m) {
                o.put("issueNumber", m.getIssueNumber());
            }
            items.add(o);
        }
        List<Object> members = new ArrayList<>();
        for (Member m : library.getMembers().all()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", m.getId());
            o.put("name", m.getName());
            o.put("type", m.getType().name());
            members.add(o);
        }
        List<Object> loans = new ArrayList<>();
        for (Loan l : library.activeLoans()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("memberId", l.getMember().getId());
            o.put("itemId", l.getItem().getId());
            o.put("loanDate", l.getLoanDate().toString());
            loans.add(o);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("items", items);
        root.put("members", members);
        root.put("loans", loans);

        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, Json.write(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not save data to " + file, e);
        }
    }

    /** Returns an empty library if the file does not exist yet. */
    @Override
    public Library load() throws StorageException {
        Library library = new Library();
        if (!Files.exists(file)) {
            return library;
        }
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not read data from " + file, e);
        }
        Map<String, Object> root;
        try {
            root = asObject(Json.parse(text), "document");
        } catch (IllegalArgumentException e) {
            throw new StorageException("Corrupt data in " + file + ": " + e.getMessage());
        }

        // Items and members first, loans last (loans refer to both).
        loadSection(library, root, "items", "item", (lib, o) -> lib.addItem(createItem(o)));
        loadSection(library, root, "members", "member", (lib, o) -> lib.registerMember(
                new Member(str(o, "id"), str(o, "name"), MembershipType.valueOf(str(o, "type")))));
        loadSection(library, root, "loans", "loan", (lib, o) -> lib.restoreLoan(
                str(o, "memberId"), str(o, "itemId"), LocalDate.parse(str(o, "loanDate"))));
        return library;
    }

    @FunctionalInterface
    private interface RecordLoader {
        void load(Library library, Map<String, Object> record) throws LibraryException;
    }

    private void loadSection(Library library, Map<String, Object> root, String key, String label,
                             RecordLoader loader) throws StorageException {
        Object section = root.get(key);
        if (section == null) {
            return;
        }
        if (!(section instanceof List<?> list)) {
            throw new StorageException("Corrupt data in " + file + ": '" + key + "' must be an array");
        }
        int n = 0;
        for (Object element : list) {
            n++;
            try {
                loader.load(library, asObject(element, label));
            } catch (LibraryException | ValidationException | DateTimeParseException
                     | IllegalArgumentException e) {
                throw new StorageException("Corrupt data in " + file + " at " + label + " #" + n
                        + ": " + e.getMessage());
            }
        }
    }

    private LibraryItem createItem(Map<String, Object> o) {
        String id = str(o, "id");
        String title = str(o, "title");
        int year = integer(o, "year");
        String type = str(o, "type");
        return switch (type) {
            case "BOOK" -> new Book(id, title, year, str(o, "author"));
            case "DVD" -> new Dvd(id, title, year, integer(o, "durationMinutes"));
            case "MAGAZINE" -> new Magazine(id, title, year, integer(o, "issueNumber"));
            default -> throw new IllegalArgumentException("Unknown item type '" + type + "'");
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object v, String what) {
        if (!(v instanceof Map)) {
            throw new IllegalArgumentException("Expected a JSON object for " + what);
        }
        return (Map<String, Object>) v;
    }

    private static String str(Map<String, Object> o, String key) {
        Object v = o.get(key);
        if (!(v instanceof String s)) {
            throw new IllegalArgumentException("Missing or non-text field '" + key + "'");
        }
        return s;
    }

    private static int integer(Map<String, Object> o, String key) {
        Object v = o.get(key);
        if (!(v instanceof Long l) || l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Missing or non-integer field '" + key + "'");
        }
        return l.intValue();
    }
}
