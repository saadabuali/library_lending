# UML Class Diagram

Written in [Mermaid](https://mermaid.js.org/) – GitHub and GitLab render it automatically
when you open this file. `+` = public, `-` = private, `#` = protected.

```mermaid
classDiagram
    direction TB

    class Identifiable {
        <<interface>>
        +getId() String
    }
    class JsonSerializable {
        <<interface>>
        +toJson() Map
    }
    class Lendable {
        <<interface>>
        +getLoanPeriodDays() int
        +getDailyLateFee() double
        +getTypeName() String
    }
    class LibraryItem {
        <<abstract>>
        -id : String
        -title : String
        -year : int
        -available : boolean
        #LibraryItem(id, title, year)
        +markBorrowed()
        +markReturned()
        +isAvailable() boolean
        +getDetails()* String
        +toJson() Map
        #addJsonFields(json)* void
        +compareTo(other) int
    }
    class Book {
        -author : String
        +getLoanPeriodDays() int
        +getDailyLateFee() double
    }
    class Dvd {
        -durationMinutes : int
        +getLoanPeriodDays() int
        +getDailyLateFee() double
    }
    class Magazine {
        -issueNumber : int
        +getLoanPeriodDays() int
        +getDailyLateFee() double
    }
    class Member {
        -id : String
        -name : String
        -type : MembershipType
        -activeLoans : List~Loan~
        +MAX_LOANS : int
        +canBorrowMore() boolean
        +addLoan(loan)
        +removeLoan(loan)
        +getActiveLoans() List~Loan~
        +toJson() Map
    }
    class MembershipType {
        <<enumeration>>
        STANDARD
        STUDENT
    }
    class Loan {
        -loanDate : LocalDate
        -dueDate : LocalDate
        +daysOverdue(returnDate) int
        +toJson() Map
    }
    class Registry~T extends Identifiable~ {
        -entries : List~T~
        +add(entry) boolean
        +getById(id) T
        +findAll(filter) List~T~
        +sorted(comparator) List~T~
    }

    class Library {
        -feePolicy : FeePolicy
        -listeners : List~LibraryListener~
        +addItem(item)
        +registerMember(member)
        +checkOut(memberId, itemId, date) Loan
        +returnItem(itemId, date) ReturnReceipt
        +setFeePolicy(policy)
        +addListener(listener)
        +search(keyword) List~LibraryItem~
    }
    class FeePolicy {
        <<interface>>
        +calculateFee(item, member, daysOverdue) double
    }
    class StandardFeePolicy
    class StudentDiscountFeePolicy
    class AmnestyFeePolicy
    class LibraryListener {
        <<interface>>
        +onEvent(event)
    }
    class ConsoleNotifier
    class ActivityLog
    class LibraryEvent {
        <<record>>
    }
    class ReturnReceipt {
        <<record>>
        +daysOverdue : int
        +fee : double
    }

    class LibraryStorage {
        <<interface>>
        +save(library)
        +load() Library
    }
    class JsonFileStorage {
        -file : Path
    }
    class ConsoleUI {
        -in : Scanner
        +run()
    }
    class Main {
        +main(args)$
    }

    class LibraryException {
        <<checked>>
    }
    class ItemUnavailableException
    class LoanLimitExceededException
    class EntityNotFoundException
    class StorageException {
        <<checked>>
    }
    class ValidationException {
        <<unchecked>>
    }

    Lendable <|.. LibraryItem
    Identifiable <|.. LibraryItem
    JsonSerializable <|.. LibraryItem
    JsonSerializable <|.. Member
    JsonSerializable <|.. Loan
    Identifiable <|.. Member
    LibraryItem <|-- Book
    LibraryItem <|-- Dvd
    LibraryItem <|-- Magazine

    FeePolicy <|.. StandardFeePolicy
    FeePolicy <|.. StudentDiscountFeePolicy
    FeePolicy <|.. AmnestyFeePolicy
    LibraryListener <|.. ConsoleNotifier
    LibraryListener <|.. ActivityLog
    LibraryStorage <|.. JsonFileStorage

    LibraryException <|-- ItemUnavailableException
    LibraryException <|-- LoanLimitExceededException
    LibraryException <|-- EntityNotFoundException

    Library o-- "1" Registry : items / members
    Registry "1" o-- "0..*" Identifiable : entries
    Member "1" *-- "0..*" Loan : activeLoans
    Loan "0..*" --> "1" LibraryItem : item
    Loan "0..*" --> "1" Member : member
    Member --> "1" MembershipType
    Library --> "1" FeePolicy : current policy (Strategy)
    Library --> "0..*" LibraryListener : notifies (Observer)
    Library ..> LibraryEvent : creates
    Library ..> ReturnReceipt : returns
    JsonFileStorage ..> Library : saves / loads
    ConsoleUI --> "1" Library
    ConsoleUI --> "1" LibraryStorage
    Main ..> ConsoleUI : creates
```

## Multiplicities in words

| Relationship | Meaning |
|---|---|
| Library ◇— Registry | The library **aggregates** one item registry and one member registry. |
| Member ◆— Loan (1 to 0..*) | **Composition**: a member owns their active loans; a loan never outlives its member. |
| Loan —> LibraryItem (many to 1) | An item can appear in at most one *active* loan, but many loans over time. |
| Library —> FeePolicy (1) | Exactly one fee policy is active; it can be swapped at runtime. |
| Library —> LibraryListener (0..*) | Any number of observers. |
