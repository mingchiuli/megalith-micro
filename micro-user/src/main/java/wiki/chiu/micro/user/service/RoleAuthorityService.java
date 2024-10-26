package wiki.chiu.micro.user.service;


import wiki.chiu.micro.user.vo.RoleAuthorityVo;

import java.util.List;

public interface RoleAuthorityService {

    List<String> getAuthoritiesByRoleCodes(String roleCode);

    List<RoleAuthorityVo> getAuthoritiesInfo(Long roleId);

    void saveAuthority(Long roleId, List<Long> authorityIds);
}
