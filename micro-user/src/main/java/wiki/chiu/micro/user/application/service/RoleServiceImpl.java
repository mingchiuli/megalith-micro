package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.error.ExceptionMessage.ROLE_NOT_EXIST;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.model.RoleDraft;
import wiki.chiu.micro.user.application.model.RoleExport;
import wiki.chiu.micro.user.application.model.RoleView;
import wiki.chiu.micro.user.application.port.in.RoleService;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.RoleWriter;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleDataPermission;
import wiki.chiu.micro.user.domain.RoleMenu;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
public class RoleServiceImpl implements RoleService {

    private final RoleReader roleRepository;

    private final RoleMenuReader roleMenuReader;

    private final UserRoleReader userRoleReader;

    private final RoleDataPermissionReader roleDataPermissionRepository;

    private final RoleWriter roleWrapper;

    private final AuthorizationQueryService authorizationQueries;

    public RoleServiceImpl(
        RoleReader roleRepository,
        RoleMenuReader roleMenuReader,
        UserRoleReader userRoleReader,
        RoleDataPermissionReader roleDataPermissionRepository,
        RoleWriter roleWrapper,
        AuthorizationQueryService authorizationQueries) {
        this.roleRepository = roleRepository;
        this.roleMenuReader = roleMenuReader;
        this.userRoleReader = userRoleReader;
        this.roleDataPermissionRepository = roleDataPermissionRepository;
        this.roleWrapper = roleWrapper;
        this.authorizationQueries = authorizationQueries;
    }

    @Override
    public RoleView info(Long id) {
        Role role =
            roleRepository.findById(id).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));

        return new RoleView(role, permissionsOf(roleDataPermissionRepository.findByRoleId(role.id())));
    }

    @Override
    public PageAdapter<RoleView> getPage(Integer currentPage, Integer size) {
        PageAdapter<Role> page = roleRepository.findPage(currentPage, size);

        List<Long> ids = page.content().stream().map(Role::id).toList();

        if (ids.isEmpty()) {
            return emptyPage(page);
        }

        List<RoleMenu> roleMenus = roleMenuReader.findByRoleIdIn(ids);
        List<RoleDataPermission> dataPermissions = roleDataPermissionRepository.findByRoleIdIn(ids);

        Map<Long, LocalDateTime> merged =
            merge(
                page.content().stream().collect(Collectors.toMap(Role::id, Role::updated)),
                roleMenus.stream()
                    .collect(
                        Collectors.toMap(
                            RoleMenu::roleId,
                            RoleMenu::updated,
                            (left, right) -> left.isAfter(right) ? left : right)),
                dataPermissions.stream()
                    .collect(
                        Collectors.toMap(
                            RoleDataPermission::roleId,
                            RoleDataPermission::updated,
                            (left, right) -> left.isAfter(right) ? left : right)));

        Map<Long, List<DataPermissionEnum>> permissionsByRole =
            dataPermissions.stream()
                .collect(
                    Collectors.groupingBy(
                        RoleDataPermission::roleId,
                        Collectors.mapping(
                            RoleDataPermission::permission,
                            Collectors.collectingAndThen(
                                Collectors.toList(),
                                permissions -> permissions.stream().sorted().toList()))));

        List<RoleView> content =
            page.content().stream()
                .map(
                    role ->
                        new RoleView(
                            role.withUpdated(merged.get(role.id())),
                            permissionsByRole.getOrDefault(role.id(), List.of())))
                .toList();

        return PageAdapter.<RoleView>builder()
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
    public void saveOrUpdate(RoleDraft role) {
        Role dealRole =
            role.id() == null
                ? Role.blank()
                : roleRepository.findById(role.id()).orElseGet(Role::blank);
        String previousCode = dealRole.code();
        Role merged = role.mergeInto(dealRole);
        List<String> affectedCodes =
            Stream.of(previousCode, merged.code())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        roleWrapper.saveOrUpdate(merged, affectedCodes);
    }

    @Override
    public void delete(List<Long> ids) {
        List<String> roles =
            roleRepository.findAllById(ids).stream().map(Role::code).distinct().toList();
        roleWrapper.delete(ids, roles);
    }

    @Override
    public RoleExport export() {
        List<Role> roles = roleRepository.findAll();
        List<UserRole> userRoles = userRoleReader.findAll();
        List<RoleDataPermission> dataPermissions = roleDataPermissionRepository.findAll();

        return new RoleExport(roles, userRoles, dataPermissions);
    }

    @Override
    public List<Role> getValidAll() {
        return roleRepository.findByStatus(StatusEnum.NORMAL.getCode());
    }

    @Override
    public List<Role> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        return roleRepository.findByCodeInAndStatus(roles, status);
    }

    @Override
    public List<RoleAuthorization> findAllRoleAuthorizations() {
        return authorizationQueries.findAllRoleAuthorizations();
    }

    @Override
    public List<RoleAuthorization> findRoleAuthorizations(List<Long> roleIds) {
        return authorizationQueries.findRoleAuthorizations(roleIds);
    }

    private static List<DataPermissionEnum> permissionsOf(List<RoleDataPermission> permissions) {
        return permissions.stream()
            .map(RoleDataPermission::permission)
            .sorted()
            .toList();
    }

    private static PageAdapter<RoleView> emptyPage(PageAdapter<Role> page) {
        return PageAdapter.<RoleView>builder()
            .content(List.of())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    @SafeVarargs
    private static Map<Long, LocalDateTime> merge(Map<Long, LocalDateTime>... maps) {
        Map<Long, LocalDateTime> merged = new HashMap<>();
        for (Map<Long, LocalDateTime> map : maps) {
            map.forEach((key, value) -> merged.merge(key, value, (l, r) -> l.isAfter(r) ? l : r));
        }
        return merged;
    }
}
