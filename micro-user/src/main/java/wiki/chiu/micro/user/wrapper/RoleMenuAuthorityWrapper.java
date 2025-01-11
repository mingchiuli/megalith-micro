package wiki.chiu.micro.user.wrapper;

import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RoleMenuAuthorityWrapper {

    private final RoleRepository roleRepository;

    private final RoleMenuRepository roleMenuRepository;

    private final MenuAuthorityRepository menuAuthorityRepository;

    public RoleMenuAuthorityWrapper(RoleRepository roleRepository, RoleMenuRepository roleMenuRepository, MenuAuthorityRepository menuAuthorityRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.menuAuthorityRepository = menuAuthorityRepository;
    }

    @Transactional
    public void delete(List<Long> ids, List<Long> menuIds) {
        roleRepository.deleteAllById(ids);
        roleMenuRepository.deleteAllByRoleId(ids);
        menuAuthorityRepository.deleteAllByMenuId(menuIds);
    }
}
