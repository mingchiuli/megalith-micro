package org.chiu.micro.user.service;


import org.chiu.micro.user.vo.RoleAuthorityVo;

import java.util.List;

public interface RoleAuthorityService {

    List<String> getAuthoritiesByRoleCodes(String roleCode);

    List<RoleAuthorityVo> getAuthoritiesInfo(Long roleId);

    void saveAuthority(Long roleId, List<Long> authorityIds);
}
