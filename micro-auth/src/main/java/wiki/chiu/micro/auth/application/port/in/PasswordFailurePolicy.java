package wiki.chiu.micro.auth.application.port.in;

/**
 * Counts failed password attempts and locks the account once the configured budget is spent.
 */
public interface PasswordFailurePolicy {

    /**
     * Records one failed password attempt.
     *
     * @param userId the account that failed
     * @return true when the account reached the failure budget and must be locked
     */
    boolean recordFailure(Long userId);
}
