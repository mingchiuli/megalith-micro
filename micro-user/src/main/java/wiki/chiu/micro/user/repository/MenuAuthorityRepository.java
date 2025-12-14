package wiki.chiu.micro.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

import java.util.List;

public interface MenuAuthorityRepository extends JpaRepository<MenuAuthorityEntity, Long> {

    void deleteByMenuId(Long menuId);

    List<MenuAuthorityEntity> findByMenuId(Long menuId);

    void deleteByAuthorityIdIn(List<Long> ids);

    void deleteByAuthorityId(Long authorityId);
}
