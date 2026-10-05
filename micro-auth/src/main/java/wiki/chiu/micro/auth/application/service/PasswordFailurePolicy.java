package wiki.chiu.micro.auth.application.service;

import wiki.chiu.micro.auth.application.port.out.PasswordFailureStore;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

/**
 * Counts failed password attempts and locks the account once the configured budget is spent. Shared
 * by the password-authentication use case, so it is an application collaborator rather than a port.
 */
public class PasswordFailurePolicy {

    private final PasswordFailureStore failures;

    private final UserDirectory users;

    private final int maxAttempts;

    public PasswordFailurePolicy(
        PasswordFailureStore failures, UserDirectory users, int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("password failure budget must be positive");
        }
        this.failures = failures;
        this.users = users;
        this.maxAttempts = maxAttempts;
    }

    public boolean recordFailure(Long userId) {
        if (failures.recordFailure(userId) < maxAttempts) {
            return false;
        }
        users.lockAfterPasswordFailures(userId);
        return true;
    }
}
