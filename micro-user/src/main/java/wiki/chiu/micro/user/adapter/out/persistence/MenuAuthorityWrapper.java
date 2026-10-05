package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.common.enums.AuthTypeEnum;
import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.AuthorityRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.application.port.out.AuthorityWriter;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;

@Component
public class MenuAuthorityWrapper implements AuthorityWriter {

    private final MenuAuthorityRepository menuAuthorityRepository;

    private final AuthorityRepository authorityRepository;
    private final AuthCacheEvictionOutbox cacheEvictions;

    public MenuAuthorityWrapper(
        MenuAuthorityRepository menuAuthorityRepository,
        AuthorityRepository authorityRepository,
        AuthCacheEvictionOutbox cacheEvictions) {
        this.menuAuthorityRepository = menuAuthorityRepository;
        this.authorityRepository = authorityRepository;
        this.cacheEvictions = cacheEvictions;
    }

    @Transactional
    @Override
    public void saveAuthority(
        Long menuId, List<MenuAuthority> menuAuthorities, List<Long> roleIds) {
        menuAuthorityRepository.deleteByMenuId(menuId);
        menuAuthorityRepository.saveAll(
            menuAuthorities.stream()
                .map(authority -> UserPersistenceMapper.toEntity(authority.onMenu(menuId)))
                .toList());
        enqueueRoleEviction(roleIds, false);
    }

    @Transactional
    @Override
    public void deleteAuthorities(List<Long> ids, List<Long> roleIds) {
        authorityRepository.deleteAllByIdInBatch(ids);
        menuAuthorityRepository.deleteByAuthorityIdIn(ids);
        enqueueRoleEviction(roleIds, true);
    }

    @Transactional
    @Override
    public void saveAuthorityEntity(Authority authority, List<Long> roleIds) {
        Long authorityId = authority.id();
        if (authorityId != null && AuthTypeEnum.WHITE_LIST.getCode().equals(authority.type())) {
            menuAuthorityRepository.deleteByAuthorityId(authorityId);
        }
        authorityRepository.save(UserPersistenceMapper.toEntity(authority));
        enqueueRoleEviction(roleIds, true);
    }

    private void enqueueRoleEviction(List<Long> roleIds, boolean evictRoutes) {
        cacheEvictions.enqueue(List.of(), roleIds, List.of(), false, evictRoutes);
    }
}
