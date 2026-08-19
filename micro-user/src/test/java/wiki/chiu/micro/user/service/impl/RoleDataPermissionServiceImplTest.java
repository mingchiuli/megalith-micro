package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.user.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.wrapper.RoleDataPermissionWrapper;

@ExtendWith(MockitoExtension.class)
class RoleDataPermissionServiceImplTest {

  @Mock private RoleRepository roles;
  @Mock private RoleDataPermissionRepository dataPermissions;
  @Mock private RoleDataPermissionWrapper wrapper;
  @InjectMocks private RoleDataPermissionServiceImpl service;

  @Test
  void returnsDistinctSortedPermissions() {
    when(roles.findById(7L)).thenReturn(Optional.of(RoleEntity.builder().id(7L).build()));
    when(dataPermissions.findByRoleId(7L))
        .thenReturn(
            List.of(
                permission(7L, DataPermissionEnum.BLOG_EDIT_ALL),
                permission(7L, DataPermissionEnum.BLOG_VIEW_ALL),
                permission(7L, DataPermissionEnum.BLOG_EDIT_ALL)));

    assertEquals(
        List.of(DataPermissionEnum.BLOG_VIEW_ALL, DataPermissionEnum.BLOG_EDIT_ALL),
        service.getDataPermissions(7L));
  }

  @Test
  void preparesDistinctSortedEntitiesBeforeSaving() {
    when(roles.findById(7L)).thenReturn(Optional.of(RoleEntity.builder().id(7L).build()));

    service.saveDataPermissions(
        7L,
        List.of(
            DataPermissionEnum.BLOG_DELETE_ALL,
            DataPermissionEnum.BLOG_VIEW_ALL,
            DataPermissionEnum.BLOG_DELETE_ALL));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<RoleDataPermissionEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(wrapper).saveDataPermissions(org.mockito.ArgumentMatchers.eq(7L), captor.capture());
    assertEquals(
        List.of(DataPermissionEnum.BLOG_VIEW_ALL, DataPermissionEnum.BLOG_DELETE_ALL),
        captor.getValue().stream().map(RoleDataPermissionEntity::permission).toList());
  }

  @Test
  void rejectsMissingRoleBeforeWriting() {
    when(roles.findById(9L)).thenReturn(Optional.empty());

    assertThrows(
        MissException.class,
        () -> service.saveDataPermissions(9L, List.of(DataPermissionEnum.BLOG_VIEW_ALL)));

    verify(wrapper, never())
        .saveDataPermissions(
            org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyList());
  }

  private RoleDataPermissionEntity permission(Long roleId, DataPermissionEnum dataPermission) {
    return new RoleDataPermissionEntity(roleId, dataPermission);
  }
}
