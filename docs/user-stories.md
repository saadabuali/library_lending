# User Stories

**Users:** librarians who run a small library desk and members who borrow items.

| # | As a... | I want to... | So that... | Acceptance criteria |
|---|---|---|---|---|
| 1 | librarian | add books, DVDs and magazines | the catalogue is complete | Each type needs its own details; invalid input (blank title, bad year) shows an error. |
| 2 | librarian | register members (standard or student) | I know who may borrow | Ids are generated; names cannot be blank. |
| 3 | librarian | check an item out to a member | loans are tracked | Fails if the item is already on loan or the member has 3 loans. Due date depends on the item type. |
| 4 | librarian | return an item and see any late fee | overdue items are charged correctly | Fee = days late × daily rate, changed by the active fee policy. |
| 5 | librarian | switch the late-fee policy | I can run discounts or amnesty weeks | Switching takes effect immediately without restarting. |
| 6 | librarian | list items sorted by title or year | I can browse the catalogue | Both orderings available. |
| 7 | librarian | search by title / author / details | I can find an item quickly | Case-insensitive keyword search. |
| 8 | librarian | see all active loans ordered by due date | I know what is overdue soon | List sorted by due date. |
| 9 | librarian | see recent activity | I can review what happened this session | Every change creates a log entry. |
| 10 | librarian | have my data saved automatically | nothing is lost between runs | Data is written after each change and restored on start; a corrupt file gives a clear error. |
