package wiki.chiu.micro.auth.application.service;

import wiki.chiu.micro.auth.application.port.in.PasswordFailurePolicy;
import wiki.chiu.micro.auth.application.port.out.PasswordFailureStore;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

public class PasswordFailurePolicyServiceImpl implements PasswordFailurePolicy {

    private final PasswordFailureStore failures;

    private final UserDirectory users;

    private final int maxAttempts;

    public PasswordFailurePolicyServiceImpl(
        PasswordFailureStore failures, UserDirectory users, int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("password failure budget must be positive");
        }
        this.failures = failures;
        this.users = users;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean recordFailure(Long userId) {
        if (failures.recordFailure(userId) < maxAttempts) {
            return false;
        }
        users.lockAfterPasswordFailures(userId);
        return true;
    }
}
