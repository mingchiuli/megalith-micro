package wiki.chiu.micro.auth.application.model;

/**
 * The token pair handed to a freshly authenticated browser.
 */
public record SessionTokens(String accessToken, String refreshToken) {
}
