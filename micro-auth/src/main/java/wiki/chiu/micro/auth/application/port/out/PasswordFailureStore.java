package wiki.chiu.micro.auth.application.port.out;

/**
 * Keeps the sliding count of failed password attempts per account.
 */
public interface PasswordFailureStore {

    /**
     * @return the number of failures inside the sliding window, including this one
     */
    long recordFailure(Long userId);

    void clear(Long userId);
}
