package wiki.chiu.micro.user.application.port.out;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;

import wiki.chiu.micro.common.exception.MissException;

public interface RegistrationTokenStore {

    String issue(String username);

    boolean exists(String token);

    default void requireValid(String token) {
        if (!exists(token)) {
            throw new MissException(NO_AUTH);
        }
    }

    void consumeForUsername(String token, String username);
}
