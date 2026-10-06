package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.user.application.model.Page;
import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.model.RoleDraft;
import wiki.chiu.micro.user.application.model.RoleExport;
import wiki.chiu.micro.user.application.model.RoleView;
import wiki.chiu.micro.user.domain.Role;

public interface RoleService {

    RoleView info(Long id);

    Page<RoleView> getPage(Integer current, Integer size);

    List<Role> getValidAll();

    List<Role> findByRoleCodeInAndStatus(List<String> roles, Integer status);

    void saveOrUpdate(RoleDraft role);

    void delete(List<Long> ids);

    List<RoleAuthorization> findAllRoleAuthorizations();

    List<RoleAuthorization> findRoleAuthorizations(List<Long> roleIds);

    RoleExport export();
}
