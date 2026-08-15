package wiki.chiu.micro.user.wrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

class RoleMenuAuthorityWrapperTest {

  @Test
  void deletePerformsOnlyWritesWithPreparedEvictionMetadata() {
    MenuRepository menus = mock(MenuRepository.class);
    MenuAuthorityRepository menuAuthorities = mock(MenuAuthorityRepository.class);
    RoleMenuRepository roleMenus = mock(RoleMenuRepository.class);
    AuthCacheEvictionOutbox cacheEvictions = mock(AuthCacheEvictionOutbox.class);
    RoleMenuAuthorityWrapper wrapper =
        new RoleMenuAuthorityWrapper(menus, menuAuthorities, roleMenus, cacheEvictions);

    wrapper.deleteMenu(3L, List.of(7L), List.of("admin"));

    verify(menus).deleteById(3L);
    verify(menuAuthorities).deleteByMenuId(3L);
    verify(roleMenus).deleteByMenuId(3L);
    verify(cacheEvictions).enqueue(List.of(), List.of(7L), List.of("admin"), true, false);
  }
}
