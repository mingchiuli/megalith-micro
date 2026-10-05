package wiki.chiu.micro.auth.application.model;

/**
 * The outcome of comparing a presented login code with the stored one.
 */
public enum CodeVerification {
    ACCEPTED,
    NOT_FOUND,
    MISMATCH,
    EXPIRED,
    ATTEMPTS_EXCEEDED
}
