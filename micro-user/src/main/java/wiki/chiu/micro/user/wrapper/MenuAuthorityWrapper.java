package wiki.chiu.micro.user.wrapper;

import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class MenuAuthorityWrapper {

    private final MenuAuthorityRepository menuAuthorityRepository;

    private final AuthorityRepository authorityRepository;

    public MenuAuthorityWrapper(MenuAuthorityRepository menuAuthorityRepository, AuthorityRepository authorityRepository) {
        this.menuAuthorityRepository = menuAuthorityRepository;
        this.authorityRepository = authorityRepository;
    }

    @Transactional
    public void saveAuthority(Long menuId, List<MenuAuthorityEntity> menuAuthorityEntities) {
        menuAuthorityRepository.deleteByMenuId(menuId);
        menuAuthorityRepository.saveAll(menuAuthorityEntities);
    }

    @Transactional
    public void deleteAuthorities(List<Long> ids) {
        authorityRepository.deleteAllById(ids);
        menuAuthorityRepository.deleteByAuthorityIdIn(ids);
    }

    @Transactional
    public void authorityEntitySave(AuthorityEntity authorityEntity) {
        Long authorityId = authorityEntity.getId();
        if (authorityId != null && AuthTypeEnum.WHITE_LIST.getCode().equals(authorityEntity.getType())) {
            menuAuthorityRepository.deleteByAuthorityId(authorityId);
        }
        authorityRepository.save(authorityEntity);
    }
}
