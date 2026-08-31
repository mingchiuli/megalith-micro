package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.List;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.user.application.port.in.RoleDataPermissionService;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionWriter;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;

@Service
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
            .map(RoleDataPermissionEntity::permission)
            .distinct()
            .sorted()
            .toList();
    }

    @Override
    public void saveDataPermissions(Long roleId, List<DataPermissionEnum> dataPermissions) {
        requireRole(roleId);
        List<RoleDataPermissionEntity> entities =
            dataPermissions.stream()
                .distinct()
                .sorted()
                .map(permission -> new RoleDataPermissionEntity(roleId, permission))
                .toList();
        roleDataPermissionWrapper.saveDataPermissions(roleId, entities);
    }

    private void requireRole(Long roleId) {
        roleRepository.findById(roleId).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
    }
}
