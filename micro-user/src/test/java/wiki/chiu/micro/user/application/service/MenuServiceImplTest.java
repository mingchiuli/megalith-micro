package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.user.adapter.out.persistence.RoleMenuAuthorityWrapper;
import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.RoleEntity;

class MenuServiceImplTest {

  @Test
  void deleteRejectsMenuWithChildrenBeforeTheWrapperTransaction() {
    MenuReader menuRepository = mock(MenuReader.class);
    RoleReader roleRepository = mock(RoleReader.class);
    RoleMenuAuthorityWrapper wrapper = mock(RoleMenuAuthorityWrapper.class);
    MenuServiceImpl service =
        new MenuServiceImpl(
            menuRepository, mock(RoleMenuReader.class), wrapper, roleRepository);
    when(menuRepository.existsByParentId(3L)).thenReturn(true);

    BaseException exception = assertThrows(BaseException.class, () -> service.delete(3L));

    assertEquals("先删除子菜单，不允许直接删除父菜单", exception.getMessage());
    verify(roleRepository, never()).findAll();
    verify(wrapper, never()).deleteMenu(3L, List.of(), List.of());
  }

  @Test
  void deletePreparesRoleMetadataBeforeTheWrapperTransaction() {
    MenuReader menuRepository = mock(MenuReader.class);
    RoleReader roleRepository = mock(RoleReader.class);
    RoleMenuAuthorityWrapper wrapper = mock(RoleMenuAuthorityWrapper.class);
    MenuServiceImpl service =
        new MenuServiceImpl(
            menuRepository, mock(RoleMenuReader.class), wrapper, roleRepository);
    when(roleRepository.findAll())
        .thenReturn(
            List.of(
                RoleEntity.builder().id(7L).code("admin").build(),
                RoleEntity.builder().id(8L).code("reader").build()));

    service.delete(3L);

    verify(wrapper).deleteMenu(3L, List.of(7L, 8L), List.of("admin", "reader"));
  }
}
