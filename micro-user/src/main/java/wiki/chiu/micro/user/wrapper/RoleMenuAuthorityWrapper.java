package wiki.chiu.micro.user.wrapper;

import wiki.chiu.micro.user.repository.RoleAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RoleMenuAuthorityWrapper {

    private final RoleRepository roleRepository;

    private final RoleAuthorityRepository roleAuthorityRepository;

    private final RoleMenuRepository roleMenuRepository;

    public RoleMenuAuthorityWrapper(RoleRepository roleRepository, RoleAuthorityRepository roleAuthorityRepository, RoleMenuRepository roleMenuRepository) {
        this.roleRepository = roleRepository;
        this.roleAuthorityRepository = roleAuthorityRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    @Transactional
    public void delete(List<Long> ids) {
        roleRepository.deleteAllById(ids);
        roleMenuRepository.deleteAllByRoleId(ids);
        roleAuthorityRepository.deleteAllByRoleId(ids);
    }
}
