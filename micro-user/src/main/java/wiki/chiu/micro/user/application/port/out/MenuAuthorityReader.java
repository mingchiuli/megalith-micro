package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;

public interface MenuAuthorityReader {

  List<MenuAuthorityEntity> findAll();

  List<MenuAuthorityEntity> findByMenuId(Long menuId);

  List<MenuAuthorityEntity> findByMenuIdIn(List<Long> menuIds);
}
