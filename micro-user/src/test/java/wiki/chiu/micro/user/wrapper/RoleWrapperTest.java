package wiki.chiu.micro.user.wrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

class RoleWrapperTest {

  @Test
  void savingRoleDoesNotReplaceDataPermissions() {
    RoleRepository roles = mock(RoleRepository.class);
    RoleMenuRepository roleMenus = mock(RoleMenuRepository.class);
    UserRoleRepository userRoles = mock(UserRoleRepository.class);
    RoleDataPermissionRepository dataPermissions = mock(RoleDataPermissionRepository.class);
    AuthCacheEvictionOutbox cacheEvictions = mock(AuthCacheEvictionOutbox.class);
    RoleWrapper wrapper =
        new RoleWrapper(roles, roleMenus, userRoles, dataPermissions, cacheEvictions);
    RoleEntity role = RoleEntity.builder().id(7L).code("editor").build();
    when(roles.save(role)).thenReturn(role);

    wrapper.saveOrUpdate(role, List.of("editor"));

    verify(dataPermissions, never()).deleteByRoleId(7L);
    verify(dataPermissions, never()).flush();
    verify(cacheEvictions).enqueue(List.of(), List.of(7L), List.of("editor"), true, false);
  }
}
