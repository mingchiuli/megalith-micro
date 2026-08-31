package wiki.chiu.micro.auth.application.port.out;

public interface VerificationCodeStore {

    boolean exists(String key);

    void save(String key, String code);
}
