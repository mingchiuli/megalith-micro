package wiki.chiu.micro.user.service;

import java.util.List;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;

public interface MenuAuthorityService {

  void saveAuthority(Long menuId, List<Long> authorityIds);

  List<MenuAuthorityVo> getAuthoritiesInfo(Long menuId);
}
