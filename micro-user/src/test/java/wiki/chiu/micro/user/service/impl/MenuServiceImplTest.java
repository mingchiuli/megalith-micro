package wiki.chiu.micro.user.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.wrapper.RoleMenuAuthorityWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuServiceImplTest {

    @Test
    void deleteRejectsMenuWithChildrenBeforePersistence() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        RoleMenuAuthorityWrapper wrapper = mock(RoleMenuAuthorityWrapper.class);
        MenuServiceImpl service = new MenuServiceImpl(
                menuRepository,
                mock(RoleRepository.class),
                mock(ApplicationContext.class),
                mock(RoleMenuRepository.class),
                mock(TaskExecutor.class),
                wrapper);
        when(menuRepository.existsByParentId(3L)).thenReturn(true);

        MissException exception = assertThrows(MissException.class, () -> service.delete(3L));

        assertEquals("please delete sub menu", exception.getMessage());
        verify(wrapper, never()).deleteMenu(3L);
    }
}
