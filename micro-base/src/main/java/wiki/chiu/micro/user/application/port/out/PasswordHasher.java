package wiki.chiu.micro.user.application.port.out;

/**
 * Hashes account passwords before they are stored.
 */
public interface PasswordHasher {

    String hash(String rawPassword);
}
