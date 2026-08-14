package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.wrapper.RoleMenuAuthorityWrapper;

class MenuServiceImplTest {

  @Test
  void deleteRejectsMenuWithChildrenBeforePersistence() {
    MenuRepository menuRepository = mock(MenuRepository.class);
    RoleMenuAuthorityWrapper wrapper = mock(RoleMenuAuthorityWrapper.class);
    MenuServiceImpl service =
        new MenuServiceImpl(
            menuRepository,
            mock(RoleRepository.class),
            mock(AuthCacheEvictionOutbox.class),
            mock(RoleMenuRepository.class),
            wrapper);
    when(menuRepository.existsByParentId(3L)).thenReturn(true);

    BaseException exception = assertThrows(BaseException.class, () -> service.delete(3L));

    assertEquals("先删除子菜单，不允许直接删除父菜单", exception.getMessage());
    verify(wrapper, never()).deleteMenu(3L);
  }
}
