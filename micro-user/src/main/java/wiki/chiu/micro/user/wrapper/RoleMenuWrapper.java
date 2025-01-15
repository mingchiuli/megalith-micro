package wiki.chiu.micro.user.wrapper;

import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class RoleMenuWrapper {

    private final RoleMenuRepository roleMenuRepository;

    public RoleMenuWrapper(RoleMenuRepository roleMenuRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleMenuRepository = roleMenuRepository;
    }

    @Transactional
    public void saveMenu(Long roleId, List<RoleMenuEntity> roleMenuEntities) {
        roleMenuRepository.deleteByRoleId(roleId);
        roleMenuRepository.saveAll(roleMenuEntities);
    }
}
