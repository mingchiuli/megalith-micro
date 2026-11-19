package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.convertor.UserPersistenceConvertor;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.domain.MenuAuthority;

@Component
public class JpaMenuAuthorityReader implements MenuAuthorityReader {

    private final MenuAuthorityRepository menuAuthorities;

    public JpaMenuAuthorityReader(MenuAuthorityRepository menuAuthorities) {
        this.menuAuthorities = menuAuthorities;
    }

    @Override
    public List<MenuAuthority> findAll() {
        return UserPersistenceConvertor.toMenuAuthorities(menuAuthorities.findAll());
    }

    @Override
    public List<MenuAuthority> findByMenuId(Long menuId) {
        return UserPersistenceConvertor.toMenuAuthorities(menuAuthorities.findByMenuId(menuId));
    }

    @Override
    public List<MenuAuthority> findByMenuIdIn(List<Long> menuIds) {
        return UserPersistenceConvertor.toMenuAuthorities(menuAuthorities.findByMenuIdIn(menuIds));
    }
}
