package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.user.application.model.MenuAuthorityView;

public interface MenuAuthorityService {

    void saveAuthority(Long menuId, List<Long> authorityIds);

    List<MenuAuthorityView> getAuthoritiesInfo(Long menuId);
}
