package wiki.chiu.micro.user.adapter.out.persistence;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

class RoleDataPermissionWrapperTest {

    @Test
    void replacesPermissionsAndEvictsOnlyRoleAuthorizationSnapshot() {
        RoleDataPermissionRepository dataPermissions = mock(RoleDataPermissionRepository.class);
        AuthCacheEvictionOutbox cacheEvictions = mock(AuthCacheEvictionOutbox.class);
        RoleDataPermissionWrapper wrapper =
            new RoleDataPermissionWrapper(dataPermissions, cacheEvictions);
        List<RoleDataPermissionEntity> entities =
            List.of(new RoleDataPermissionEntity(7L, DataPermissionEnum.BLOG_VIEW_ALL));

        wrapper.saveDataPermissions(7L, entities);

        InOrder writes = inOrder(dataPermissions, cacheEvictions);
        writes.verify(dataPermissions).deleteByRoleId(7L);
        writes.verify(dataPermissions).flush();
        writes.verify(dataPermissions).saveAll(entities);
        writes.verify(cacheEvictions).enqueue(List.of(), List.of(7L), List.of(), false, false);
    }

    @Test
    void acceptsEmptyPermissionsToRestoreOwnDataOnly() {
        RoleDataPermissionRepository dataPermissions = mock(RoleDataPermissionRepository.class);
        AuthCacheEvictionOutbox cacheEvictions = mock(AuthCacheEvictionOutbox.class);
        RoleDataPermissionWrapper wrapper =
            new RoleDataPermissionWrapper(dataPermissions, cacheEvictions);

        wrapper.saveDataPermissions(7L, List.of());

        verify(dataPermissions).saveAll(List.of());
    }
}
