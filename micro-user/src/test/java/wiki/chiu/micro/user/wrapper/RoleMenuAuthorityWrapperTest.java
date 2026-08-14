package wiki.chiu.micro.user.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

class RoleMenuAuthorityWrapperTest {

  @Test
  void deleteRejectsMenuWithChildrenBeforePersistence() {
    MenuRepository menus = mock(MenuRepository.class);
    MenuAuthorityRepository menuAuthorities = mock(MenuAuthorityRepository.class);
    RoleMenuRepository roleMenus = mock(RoleMenuRepository.class);
    RoleMenuAuthorityWrapper wrapper =
        new RoleMenuAuthorityWrapper(
            menus,
            menuAuthorities,
            roleMenus,
            mock(RoleRepository.class),
            mock(AuthCacheEvictionOutbox.class));
    when(menus.existsByParentId(3L)).thenReturn(true);

    BaseException exception = assertThrows(BaseException.class, () -> wrapper.deleteMenu(3L));

    assertEquals("先删除子菜单，不允许直接删除父菜单", exception.getMessage());
    verify(menus, never()).deleteById(3L);
    verify(menuAuthorities, never()).deleteByMenuId(3L);
    verify(roleMenus, never()).deleteByMenuId(3L);
  }
}
