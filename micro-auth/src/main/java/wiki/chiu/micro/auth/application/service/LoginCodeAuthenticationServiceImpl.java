package wiki.chiu.micro.auth.application.service;

import wiki.chiu.micro.auth.application.model.CodeVerification;
import wiki.chiu.micro.auth.application.model.LoginChannel;
import wiki.chiu.micro.auth.application.port.in.LoginCodeAuthentication;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;

public class LoginCodeAuthenticationServiceImpl implements LoginCodeAuthentication {

    private final LoginCodeStore codes;

    private final int maxAttempts;

    public LoginCodeAuthenticationServiceImpl(LoginCodeStore codes, int maxAttempts) {
        this.codes = codes;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public CodeVerification authenticate(
        LoginChannel channel, String principal, String presentedCode) {
        LoginCodeStore.Verification verification =
            codes.verify(channelOf(channel), principal, presentedCode, maxAttempts);
        return verificationOf(verification);
    }

    private LoginCodeStore.LoginChannel channelOf(LoginChannel channel) {
        return switch (channel) {
            case EMAIL -> LoginCodeStore.LoginChannel.EMAIL;
            case PHONE -> LoginCodeStore.LoginChannel.PHONE;
        };
    }

    private CodeVerification verificationOf(LoginCodeStore.Verification verification) {
        return switch (verification) {
            case ACCEPTED -> CodeVerification.ACCEPTED;
            case NOT_FOUND -> CodeVerification.NOT_FOUND;
            case MISMATCH -> CodeVerification.MISMATCH;
            case EXPIRED -> CodeVerification.EXPIRED;
            case ATTEMPTS_EXCEEDED -> CodeVerification.ATTEMPTS_EXCEEDED;
        };
    }
}
