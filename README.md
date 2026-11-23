# Library Lending System

A console application for a small library desk: manage books, DVDs and magazines,
register members, lend and return items, and calculate late fees. Data is saved to a JSON file
between runs.

**Contributors:** Saad

## Problem statement
Small libraries often track loans on paper. Librarians need to know which items are available,
who has what, when it is due, and what a late return costs. This program solves that and lets the
library change its fee rules (e.g. student discount, amnesty week) without changing code.

## Prerequisites
- JDK 17 or newer
- Maven 3.8+ (IntelliJ bundles one) – or just use IntelliJ's Maven panel

## Build, test and run
```bash
mvn clean test          # build + run all JUnit tests
mvn clean package       # creates target/library-lending-1.0.0.jar
java -jar target/library-lending-1.0.0.jar          # run (data saved in ./data/library.json)
# or: mvn compile exec:java
```
In IntelliJ: open the folder as a Maven project, then run `edu.library.ui.Main` (green arrow).

## Sample usage
```
Choose: 12                      <- load sample data
Choose: 7                       <- check out
Member id: U002
Item id: D001
Date (yyyy-MM-dd, blank = today): 2026-11-01
  [notice] Peter Nagy borrowed 'The Matrix', due 2026-11-08.
Choose: 8                       <- return 3 days late
Item id: D001
Return date (yyyy-MM-dd, blank = today): 2026-11-11
  Days overdue: 3, fee (Standard): 3.00
```
To see the Strategy pattern, use option 10 to switch to "Student discount" and return another
late DVD borrowed by the student: the fee halves.

## Requirements checklist
| Requirement | Where |
|---|---|
| Encapsulation | Private fields everywhere; constructors validate (`LibraryItem`, `Member`, `Loan`); `Member.getActiveLoans()` returns an unmodifiable list |
| Abstraction | Interfaces `Lendable`, `Identifiable`, `JsonSerializable`, `FeePolicy`, `LibraryListener`, `LibraryStorage`; abstract class `LibraryItem` |
| Inheritance + polymorphism | `Book`, `Dvd`, `Magazine` extend `LibraryItem` and override loan period, late fee and details; fee policy is swapped at runtime |
| Relationships | Library ◇ Registry (aggregation), Member ◆ Loan (composition, 1 to 0..*), Loan → Item / Member (association) – see `docs/class-diagram.md` |
| Collections + generics | `ArrayList`, custom generic `Registry<T extends Identifiable>`; sorting with `Comparable` (`LibraryItem`), `Comparator` and lambdas (`Library.itemsSortedByYear`) |
| Exceptions | Custom checked: `LibraryException` and subclasses (`ItemUnavailableException`, `LoanLimitExceededException`, `EntityNotFoundException`, `StorageException`); unchecked `ValidationException` |
| Persistence | `JsonFileStorage` reads/writes `data/library.json`; each domain object writes itself through `JsonSerializable.toJson()` (no `instanceof` in the storage layer) |
| Testing | `src/test/java/edu/library/LibraryTest.java` (JUnit 4, 18 tests) |
| Build | Maven (`pom.xml`) |

## Design patterns
### Strategy (main pattern)
- **Problem:** libraries change late-fee rules (student discount, amnesty). Hard-coding `if` chains inside `Library` would mean editing it for every new rule.
- **Participants:** `FeePolicy` (strategy interface), `StandardFeePolicy`, `StudentDiscountFeePolicy`, `AmnestyFeePolicy` (concrete strategies), `Library` (context, holds the current policy and calls `calculateFee` in `returnItem`), `ConsoleUI` option 10 switches it at runtime.
- **Benefit:** new rules are new classes; `Library` stays unchanged (open/closed principle) and each policy is unit-tested separately.

### Observer (secondary pattern)
- **Participants:** `LibraryListener` (observer interface), `ConsoleNotifier` and `ActivityLog` (concrete observers), `Library` (subject; `addListener` / `fire`), `LibraryEvent` (event data).
- **Benefit:** the service does not know about printing or logging; new reactions (e.g. email) only need a new listener.

## Main design decisions
- Layers are separate packages: `domain`, `service`, `persistence`, `ui`, `exception`.
- JSON file format instead of Java serialization or pipe-separated text: human readable, easy to debug and
  safe for titles containing any character. A tiny built-in reader/writer (`Json`) keeps the project dependency-free.
- Data is saved after every change, so a crash loses nothing.
- Dates for checkout/return can be typed in so late fees can be demonstrated without waiting.

## Known limitations
See `docs/RELEASE_NOTES.md`.

## Documentation
- `docs/class-diagram.md` – UML class diagram (Mermaid, renders on GitHub/GitLab)
- `docs/user-stories.md` – user stories
- `docs/RELEASE_NOTES.md` – release notes

## License
MIT – see `LICENSE`.
