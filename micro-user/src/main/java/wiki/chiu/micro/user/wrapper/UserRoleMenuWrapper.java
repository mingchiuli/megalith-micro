package wiki.chiu.micro.user.wrapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.repository.*;

import java.util.List;


@Component
public class UserRoleMenuWrapper {

    private final RoleRepository roleRepository;

    private final RoleMenuRepository roleMenuRepository;

    private final UserRoleRepository userRoleRepository;

    public UserRoleMenuWrapper(RoleRepository roleRepository, RoleMenuRepository roleMenuRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public void deleteRole(List<Long> ids) {
        roleRepository.deleteAllById(ids);
        roleMenuRepository.deleteAllByRoleIdIn(ids);
        userRoleRepository.deleteByRoleIdIn(ids);
    }
}
