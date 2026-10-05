package wiki.chiu.micro.auth.application.port.in;

/**
 * Verifies a submitted password and applies the failure budget when it does not match.
 */
public interface PasswordAuthentication {

    /**
     * @param userId the account that is authenticating
     * @param encodedPassword the stored password hash loaded for that account
     * @param presentedPassword the password the caller submitted
     * @return true when the password matches; a failure is recorded otherwise
     */
    boolean authenticate(Long userId, String encodedPassword, String presentedPassword);
}
