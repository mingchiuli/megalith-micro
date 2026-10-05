package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.domain.RoleMenu;

@Component
public class JpaRoleMenuReader implements RoleMenuReader {

    private final RoleMenuRepository roleMenus;

    public JpaRoleMenuReader(RoleMenuRepository roleMenus) {
        this.roleMenus = roleMenus;
    }

    @Override
    public List<RoleMenu> findAll() {
        return UserPersistenceMapper.toRoleMenus(roleMenus.findAll());
    }

    @Override
    public List<RoleMenu> findByRoleIdIn(List<Long> roleIds) {
        return UserPersistenceMapper.toRoleMenus(roleMenus.findByRoleIdIn(roleIds));
    }

    @Override
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return roleMenus.findMenuIdsByRoleId(roleId);
    }
}
