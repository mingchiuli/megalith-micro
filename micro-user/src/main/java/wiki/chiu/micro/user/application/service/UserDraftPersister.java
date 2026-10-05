package wiki.chiu.micro.user.application.service;

import java.util.List;

import wiki.chiu.micro.user.application.model.UserDraft;
import wiki.chiu.micro.user.application.port.out.PasswordHasher;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserWriter;
import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * Persists a user draft together with its roles. Shared by the admin user use case and
 * self-registration, so it is an application collaborator rather than an input port.
 */
public class UserDraftPersister {

    private final UserReader users;

    private final UserWriter writer;

    private final PasswordHasher passwordHasher;

    private final RoleReader roles;

    public UserDraftPersister(
        UserReader users, UserWriter writer, PasswordHasher passwordHasher, RoleReader roles) {
        this.users = users;
        this.writer = writer;
        this.passwordHasher = passwordHasher;
        this.roles = roles;
    }

    public void save(UserDraft userDraft) {
        User dealUser = getUserEntity(userDraft);

        UserDraft userReq =
            userDraft.id() != null && !hasLength(userDraft.password())
                ? userDraft.withPassword(dealUser.password())
                : userDraft.withPassword(passwordHasher.hash(userDraft.password()));

        User user = userReq.mergeInto(dealUser);

        List<UserRole> userRoles =
            roles.findByCodeIn(userDraft.roles()).stream()
                .map(role -> new UserRole(null, null, role.id(), null, null))
                .toList();

        writer.saveOrUpdate(user, userRoles);
    }

    private User getUserEntity(UserDraft userDraft) {
        return userDraft.id() == null
            ? User.blank()
            : users.findById(userDraft.id()).orElseGet(User::blank);
    }

    private static boolean hasLength(String value) {
        return value != null && !value.isEmpty();
    }
}
