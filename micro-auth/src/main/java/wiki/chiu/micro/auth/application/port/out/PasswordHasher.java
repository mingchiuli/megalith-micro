package wiki.chiu.micro.auth.application.port.out;

/**
 * Compares a presented password with the stored hash.
 */
public interface PasswordHasher {

    /**
     * @param rawPassword the password the caller submitted
     * @param encodedPassword the stored password hash
     * @return true when the presented password matches the stored hash
     */
    boolean matches(String rawPassword, String encodedPassword);
}
