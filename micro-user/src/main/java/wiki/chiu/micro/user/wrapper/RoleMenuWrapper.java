package wiki.chiu.micro.user.wrapper;

import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class RoleMenuWrapper {

    private final RoleMenuRepository roleMenuRepository;

    private final MenuRepository menuRepository;

    public RoleMenuWrapper(RoleMenuRepository roleMenuRepository, MenuRepository menuRepository) {
        this.roleMenuRepository = roleMenuRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public void saveMenu(Long roleId, List<RoleMenuEntity> roleMenuEntities) {
        roleMenuRepository.deleteByRoleId(roleId);
        roleMenuRepository.saveAll(roleMenuEntities);
    }

    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
        roleMenuRepository.deleteByMenuId(id);
    }
}
