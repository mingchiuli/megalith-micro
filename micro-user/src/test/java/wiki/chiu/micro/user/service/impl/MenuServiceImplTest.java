package wiki.chiu.micro.user.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.wrapper.RoleMenuAuthorityWrapper;

class MenuServiceImplTest {

  @Test
  void deleteDelegatesTheTransactionalOperationToTheWrapper() {
    MenuRepository menuRepository = mock(MenuRepository.class);
    RoleMenuAuthorityWrapper wrapper = mock(RoleMenuAuthorityWrapper.class);
    MenuServiceImpl service =
        new MenuServiceImpl(menuRepository, mock(RoleMenuRepository.class), wrapper);

    service.delete(3L);

    verify(wrapper).deleteMenu(3L);
  }
}
