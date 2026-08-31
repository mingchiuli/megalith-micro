package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;

public interface MenuAuthorityRepository
    extends JpaRepository<MenuAuthorityEntity, Long>, MenuAuthorityReader {

    void deleteByMenuId(Long menuId);

    List<MenuAuthorityEntity> findByMenuId(Long menuId);

    List<MenuAuthorityEntity> findByMenuIdIn(List<Long> menuIds);

    void deleteByAuthorityIdIn(List<Long> ids);

    void deleteByAuthorityId(Long authorityId);
}
