package wiki.chiu.micro.user.application.model;

/**
 * A self-service registration as submitted by a client.
 */
public record RegistrationDraft(
    String username,
    String nickname,
    String avatar,
    String password,
    String email,
    String phone,
    String token) {

    public RegistrationDraft withPhone(String newPhone) {
        return new RegistrationDraft(username, nickname, avatar, password, email, newPhone, token);
    }
}
