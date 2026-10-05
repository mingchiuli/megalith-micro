package wiki.chiu.micro.auth.application.service;

import wiki.chiu.micro.auth.application.model.SessionTokens;
import wiki.chiu.micro.auth.application.port.in.LoginSession;
import wiki.chiu.micro.auth.application.port.out.PasswordFailureStore;
import wiki.chiu.micro.auth.application.port.out.TokenEncoder;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

public class LoginSessionServiceImpl implements LoginSession {

    private final PasswordFailureStore passwordFailures;

    private final UserDirectory users;

    private final TokenEncoder tokens;

    public LoginSessionServiceImpl(
        PasswordFailureStore passwordFailures, UserDirectory users, TokenEncoder tokens) {
        this.passwordFailures = passwordFailures;
        this.users = users;
        this.tokens = tokens;
    }

    @Override
    public SessionTokens start(Long userId, String username) {
        passwordFailures.clear(userId);
        users.updateLoginTime(username);
        return new SessionTokens(tokens.accessToken(userId), tokens.refreshToken(userId));
    }
}
