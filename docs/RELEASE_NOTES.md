# Release v1.0.0

## Completed features
- Catalogue of books, DVDs and magazines (add, list, sort by title/year, search)
- Member registration (standard / student)
- Check-out and return with due dates and late fees
- Swappable late-fee policies (Strategy) and event notifications (Observer)
- JSON-file persistence and JUnit test suite (16 tests)

## Known limitations
- Console interface only (no GUI)
- Only active loans are stored; returned loans are not kept as history
- Titles/names cannot contain the `|` character (file delimiter)
- Single user, no authentication
- Fees use `double`, which is fine for this demo but not for real money (use `BigDecimal`)
