package wiki.chiu.micro.auth.application.model;

import java.util.List;

/**
 * The account the authentication chain loads for a login name, together with the roles that count
 * for this login.
 */
public record LoginAccount(
    Long userId,
    String username,
    String password,
    Integer status,
    List<String> roleCodes) {
}
