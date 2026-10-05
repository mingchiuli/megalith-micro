package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.MenuAuthority;

public interface MenuAuthorityReader {

    List<MenuAuthority> findAll();

    List<MenuAuthority> findByMenuId(Long menuId);

    List<MenuAuthority> findByMenuIdIn(List<Long> menuIds);
}
