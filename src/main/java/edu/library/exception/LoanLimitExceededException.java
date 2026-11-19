package edu.library.exception;

/** Thrown when a member already holds the maximum number of loans. */
public class LoanLimitExceededException extends LibraryException {
    public LoanLimitExceededException(String memberName, int limit) {
        super(memberName + " has reached the loan limit of " + limit + " items.");
    }
}
