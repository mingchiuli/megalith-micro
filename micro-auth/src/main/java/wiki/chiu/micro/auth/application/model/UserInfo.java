package wiki.chiu.micro.auth.application.model;

/**
 * The signed-in user as the auth service presents it.
 */
public record UserInfo(Long id, String nickname, String avatar) {
}
