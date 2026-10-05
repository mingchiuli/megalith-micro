package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.application.port.out.RoleMenuWriter;
import wiki.chiu.micro.user.domain.RoleMenu;

@Component
public class RoleMenuWrapper implements RoleMenuWriter {

    private final RoleMenuRepository roleMenuRepository;
    private final AuthCacheEvictionOutbox cacheEvictions;

    public RoleMenuWrapper(
        RoleMenuRepository roleMenuRepository, AuthCacheEvictionOutbox cacheEvictions) {
        this.roleMenuRepository = roleMenuRepository;
        this.cacheEvictions = cacheEvictions;
    }

    @Transactional
    @Override
    public void saveMenu(Long roleId, String roleCode, List<RoleMenu> roleMenus) {
        roleMenuRepository.deleteByRoleId(roleId);
        roleMenuRepository.saveAll(
            roleMenus.stream()
                .map(roleMenu -> UserPersistenceMapper.toEntity(roleMenu.onRole(roleId)))
                .toList());
        cacheEvictions.enqueue(List.of(), List.of(roleId), List.of(roleCode), true, false);
    }
}
