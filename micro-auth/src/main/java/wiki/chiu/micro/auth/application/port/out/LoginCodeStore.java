package wiki.chiu.micro.auth.application.port.out;

/**
 * Stores the one-time login codes this service issues and verifies the presented ones.
 */
public interface LoginCodeStore {

    boolean exists(LoginChannel channel, String principal);

    void save(LoginChannel channel, String principal, String code);

    /**
     * Compares a presented code with the stored one and consumes it when it matches.
     *
     * @param channel how the code was delivered
     * @param principal the e-mail address or phone number the code was issued for
     * @param presentedCode the code the caller submitted
     * @param maxAttempts how many mismatches the code tolerates
     * @return the outcome of the comparison
     */
    Verification verify(
        LoginChannel channel, String principal, String presentedCode, int maxAttempts);

    enum LoginChannel {
        EMAIL,
        PHONE
    }

    enum Verification {
        ACCEPTED,
        NOT_FOUND,
        MISMATCH,
        EXPIRED,
        ATTEMPTS_EXCEEDED
    }
}
