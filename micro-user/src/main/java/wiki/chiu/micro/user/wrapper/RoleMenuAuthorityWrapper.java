package wiki.chiu.micro.user.wrapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;


@Component
public class RoleMenuAuthorityWrapper {

    private final MenuRepository menuRepository;

    private final MenuAuthorityRepository menuAuthorityRepository;

    private final RoleMenuRepository roleMenuRepository;

    public RoleMenuAuthorityWrapper(MenuRepository menuRepository, MenuAuthorityRepository menuAuthorityRepository, RoleMenuRepository roleMenuRepository) {
        this.menuRepository = menuRepository;
        this.menuAuthorityRepository = menuAuthorityRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
        menuAuthorityRepository.deleteByMenuId(id);
        roleMenuRepository.deleteByMenuId(id);
    }
}
