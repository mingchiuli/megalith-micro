package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.USER_NOT_EXIST;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.application.model.UserDraft;
import wiki.chiu.micro.user.application.model.UserView;
import wiki.chiu.micro.user.application.port.in.UserService;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.application.port.out.UserWriter;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
public class UserServiceImpl implements UserService {

    private final UserReader userRepository;

    private final UserWriter userRoleWrapper;

    private final RoleReader roleRepository;

    private final UserRoleReader userRoleReader;

    private final UserDraftPersister userDrafts;

    private final RoleCodeLookup roleCodes;

    public UserServiceImpl(
        UserReader userRepository,
        UserWriter userRoleWrapper,
        RoleReader roleRepository,
        UserRoleReader userRoleReader,
        UserDraftPersister userDrafts,
        RoleCodeLookup roleCodes) {
        this.userRepository = userRepository;
        this.userRoleWrapper = userRoleWrapper;
        this.roleRepository = roleRepository;
        this.userRoleReader = userRoleReader;
        this.userDrafts = userDrafts;
        this.roleCodes = roleCodes;
    }

    @Override
    public UserView findInfo(Long userId) {
        User user =
            userRepository.findById(userId).orElseThrow(() -> new MissException(USER_NOT_EXIST));

        List<String> codes = roleCodes.codesOf(userId);
        return new UserView(user, codes);
    }

    @Override
    public void saveOrUpdate(UserDraft userDraft) {
        userDrafts.save(userDraft);
    }

    @Override
    public PageAdapter<UserView> listPage(Integer currentPage, Integer size) {
        PageAdapter<User> page = userRepository.findPage(currentPage, size);

        List<Long> userIds = page.content().stream().map(User::id).toList();
        List<UserRole> userRoles = userRoleReader.findByUserIdIn(userIds);

        List<Long> roleIds = userRoles.stream().map(UserRole::roleId).toList();
        List<Role> roles = roleRepository.findAllById(roleIds);

        Map<Long, List<String>> codesByUser = codesByUser(userRoles, roles);
        Map<Long, LocalDateTime> merged =
            merge(
                page.content().stream().collect(Collectors.toMap(User::id, User::updated)),
                userRoles.stream()
                    .collect(
                        Collectors.toMap(
                            UserRole::userId,
                            UserRole::updated,
                            (left, right) -> left.isAfter(right) ? left : right)));

        List<UserView> content =
            page.content().stream()
                .map(
                    user ->
                        new UserView(
                            user.withUpdated(merged.get(user.id())),
                            codesByUser.getOrDefault(user.id(), List.of())))
                .toList();

        return PageAdapter.<UserView>builder()
            .content(content)
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    @Override
    public void deleteUsers(List<Long> ids) {
        userRoleWrapper.deleteUsers(ids);
    }

    private static Map<Long, List<String>> codesByUser(
        List<UserRole> userRoles, List<Role> roles) {
        return userRoles.stream()
            .collect(Collectors.groupingBy(UserRole::userId))
            .entrySet()
            .stream()
            .map(
                entry -> {
                    List<Long> roleIds =
                        entry.getValue().stream().map(UserRole::roleId).toList();
                    List<String> roleCodes =
                        roles.stream()
                            .filter(role -> roleIds.contains(role.id()))
                            .map(Role::code)
                            .toList();
                    return Map.entry(entry.getKey(), roleCodes);
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<Long, LocalDateTime> merge(
        Map<Long, LocalDateTime> left, Map<Long, LocalDateTime> right) {
        Map<Long, LocalDateTime> merged = new HashMap<>(left);
        right.forEach((key, value) -> merged.merge(key, value, (l, r) -> l.isAfter(r) ? l : r));
        return merged;
    }
}
