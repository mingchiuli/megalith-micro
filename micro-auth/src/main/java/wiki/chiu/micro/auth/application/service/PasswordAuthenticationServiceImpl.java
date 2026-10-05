package wiki.chiu.micro.auth.application.service;

import wiki.chiu.micro.auth.application.port.in.PasswordAuthentication;
import wiki.chiu.micro.auth.application.port.out.PasswordHasher;

public class PasswordAuthenticationServiceImpl implements PasswordAuthentication {

    private final PasswordHasher hasher;

    private final PasswordFailurePolicy passwordFailures;

    public PasswordAuthenticationServiceImpl(
        PasswordHasher hasher, PasswordFailurePolicy passwordFailures) {
        this.hasher = hasher;
        this.passwordFailures = passwordFailures;
    }

    @Override
    public boolean authenticate(Long userId, String encodedPassword, String presentedPassword) {
        if (hasher.matches(presentedPassword, encodedPassword)) {
            return true;
        }
        passwordFailures.recordFailure(userId);
        return false;
    }
}
