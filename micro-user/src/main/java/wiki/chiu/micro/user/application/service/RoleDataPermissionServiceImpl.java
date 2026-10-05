package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.error.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.List;


import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.application.port.in.RoleDataPermissionService;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionWriter;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.RoleDataPermission;

public class RoleDataPermissionServiceImpl implements RoleDataPermissionService {

    private final RoleReader roleRepository;
    private final RoleDataPermissionReader roleDataPermissionRepository;
    private final RoleDataPermissionWriter roleDataPermissionWrapper;

    public RoleDataPermissionServiceImpl(
        RoleReader roleRepository,
        RoleDataPermissionReader roleDataPermissionRepository,
        RoleDataPermissionWriter roleDataPermissionWrapper) {
        this.roleRepository = roleRepository;
        this.roleDataPermissionRepository = roleDataPermissionRepository;
        this.roleDataPermissionWrapper = roleDataPermissionWrapper;
    }

    @Override
    public List<DataPermissionEnum> getDataPermissions(Long roleId) {
        requireRole(roleId);
        return roleDataPermissionRepository.findByRoleId(roleId).stream()
            .map(RoleDataPermission::permission)
            .distinct()
            .sorted()
            .toList();
    }

    @Override
    public void saveDataPermissions(Long roleId, List<DataPermissionEnum> dataPermissions) {
        requireRole(roleId);
        List<RoleDataPermission> entities =
            dataPermissions.stream()
                .distinct()
                .sorted()
                .map(permission -> new RoleDataPermission(null, roleId, permission, null, null))
                .toList();
        roleDataPermissionWrapper.saveDataPermissions(roleId, entities);
    }

    private void requireRole(Long roleId) {
        roleRepository.findById(roleId).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
    }
}
