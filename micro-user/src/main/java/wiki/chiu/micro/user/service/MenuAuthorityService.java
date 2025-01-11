package wiki.chiu.micro.user.service;

import wiki.chiu.micro.user.vo.MenuAuthorityVo;

import java.util.List;

public interface MenuAuthorityService {

    void saveAuthority(Long menuId, List<Long> authorityIds);

    List<MenuAuthorityVo> getAuthoritiesInfo(Long menuId);
}
