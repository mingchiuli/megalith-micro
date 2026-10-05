package wiki.chiu.micro.auth.application.model;

/**
 * The account details the authentication chain needs.
 */
public record UserAccount(
    Long id,
    String username,
    String password,
    String nickname,
    String avatar,
    Integer status) {
}
