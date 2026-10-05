package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.application.port.out.MenuWriter;
import wiki.chiu.micro.user.domain.Menu;

@Component
public class RoleMenuAuthorityWrapper implements MenuWriter {

    private final MenuRepository menuRepository;

    private final MenuAuthorityRepository menuAuthorityRepository;

    private final RoleMenuRepository roleMenuRepository;
    private final AuthCacheEvictionOutbox cacheEvictions;

    public RoleMenuAuthorityWrapper(
        MenuRepository menuRepository,
        MenuAuthorityRepository menuAuthorityRepository,
        RoleMenuRepository roleMenuRepository,
        AuthCacheEvictionOutbox cacheEvictions) {
        this.menuRepository = menuRepository;
        this.menuAuthorityRepository = menuAuthorityRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.cacheEvictions = cacheEvictions;
    }

    @Transactional
    @Override
    public void saveMenus(List<Menu> menus, List<Long> roleIds, List<String> roleCodes) {
        menuRepository.saveAll(menus.stream().map(UserPersistenceMapper::toEntity).toList());
        enqueueAllRoleEviction(roleIds, roleCodes);
    }

    @Transactional
    @Override
    public void deleteMenu(Long id, List<Long> roleIds, List<String> roleCodes) {
        menuRepository.deleteById(id);
        menuAuthorityRepository.deleteByMenuId(id);
        roleMenuRepository.deleteByMenuId(id);
        enqueueAllRoleEviction(roleIds, roleCodes);
    }

    private void enqueueAllRoleEviction(List<Long> roleIds, List<String> roleCodes) {
        cacheEvictions.enqueue(List.of(), roleIds, roleCodes, true, false);
    }
}
