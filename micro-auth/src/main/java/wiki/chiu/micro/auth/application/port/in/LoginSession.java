package wiki.chiu.micro.auth.application.port.in;

import wiki.chiu.micro.auth.application.model.SessionTokens;

/**
 * The steps that follow a successful authentication: the login is recorded and the browser receives
 * a fresh token pair.
 */
public interface LoginSession {

    /**
     * Records the login of an authenticated user and issues the token pair for the browser.
     *
     * @param userId the authenticated user
     * @param username the submitted login name, recorded as the last login identity
     * @return the cookies' token values
     */
    SessionTokens start(Long userId, String username);
}
