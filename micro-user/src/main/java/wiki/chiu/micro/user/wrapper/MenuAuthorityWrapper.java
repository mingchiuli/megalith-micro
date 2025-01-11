package wiki.chiu.micro.user.wrapper;

import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class MenuAuthorityWrapper {

    private final MenuAuthorityRepository menuAuthorityRepository;

    public MenuAuthorityWrapper(MenuAuthorityRepository menuAuthorityRepository) {
        this.menuAuthorityRepository = menuAuthorityRepository;
    }

    @Transactional
    public void saveAuthority(Long menuId, List<MenuAuthorityEntity> menuAuthorityEntities) {
        menuAuthorityRepository.deleteByMenuId(menuId);
        menuAuthorityRepository.saveAll(menuAuthorityEntities);
    }
}
