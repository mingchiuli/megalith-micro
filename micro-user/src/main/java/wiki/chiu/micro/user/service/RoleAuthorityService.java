package wiki.chiu.micro.user.service;


import wiki.chiu.micro.user.vo.RoleAuthorityVo;

import java.util.List;
import java.util.Set;

public interface RoleAuthorityService {

    Set<String> getAuthoritiesByRoleCodes(String roleCode);

    List<RoleAuthorityVo> getAuthoritiesInfo(Long roleId);

    void saveAuthority(Long roleId, List<Long> authorityIds);
}
