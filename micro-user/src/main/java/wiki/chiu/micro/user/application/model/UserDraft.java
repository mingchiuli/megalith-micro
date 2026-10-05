package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.User;

/**
 * An account as submitted by a client, before it is merged with the stored state.
 */
public record UserDraft(
    Long id,
    String username,
    String nickname,
    String avatar,
    String password,
    String email,
    String phone,
    Integer status,
    List<String> roles) {

    public UserDraft withPassword(String newPassword) {
        return new UserDraft(
            id, username, nickname, avatar, newPassword, email, phone, status, roles);
    }

    public User mergeInto(User dealUser) {
        return new User(
            id,
            username,
            nickname,
            avatar,
            email,
            phone,
            password,
            status,
            null,
            dealUser.created(),
            dealUser.updated(),
            dealUser.lastLogin());
    }
}
