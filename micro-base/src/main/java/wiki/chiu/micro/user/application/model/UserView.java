package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.User;

/**
 * An account with the codes of the roles it belongs to.
 */
public record UserView(User user, List<String> roles) {

    public UserView {
        roles = List.copyOf(roles);
    }
}
