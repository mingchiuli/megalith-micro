package wiki.chiu.micro.auth.application.model;

import java.util.List;

/**
 * What the user service knows about a user's access rights.
 */
public record UserAccess(Long userId, boolean exists, Integer status, List<Long> roleIds) {

    public UserAccess {
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
    }

    public static UserAccess missing(Long userId) {
        return new UserAccess(userId, false, null, List.of());
    }
}
