package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.convertor.UserPersistenceConvertor;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuRepository;
import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.domain.Menu;

@Component
public class JpaMenuReader implements MenuReader {

    private final MenuRepository menus;

    public JpaMenuReader(MenuRepository menus) {
        this.menus = menus;
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return menus.findById(id).map(UserPersistenceConvertor::toDomain);
    }

    @Override
    public List<Menu> findAll() {
        return UserPersistenceConvertor.toMenus(menus.findAll());
    }

    @Override
    public List<Menu> findAllById(Iterable<Long> ids) {
        return UserPersistenceConvertor.toMenus(menus.findAllById(ids));
    }

    @Override
    public List<Menu> findAllByOrderByOrderNumDesc() {
        return UserPersistenceConvertor.toMenus(menus.findAllByOrderByOrderNumDesc());
    }

    @Override
    public List<Menu> findByParentId(Long parentId) {
        return UserPersistenceConvertor.toMenus(menus.findByParentId(UserPersistenceConvertor.parentReference(parentId)));
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return menus.existsByParentId(UserPersistenceConvertor.parentReference(parentId));
    }
}
