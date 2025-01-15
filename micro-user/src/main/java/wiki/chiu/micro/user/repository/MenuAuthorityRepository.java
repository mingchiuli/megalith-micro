package wiki.chiu.micro.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

import java.util.List;

public interface MenuAuthorityRepository extends JpaRepository<MenuAuthorityEntity, Long> {

    @Modifying
    @Transactional
    void deleteByMenuId(Long menuId);

    List<MenuAuthorityEntity> findByMenuId(Long menuId);

    @Modifying
    @Transactional
    void deleteByAuthorityIdIn(List<Long> ids);

    @Modifying
    @Transactional
    void deleteByAuthorityId(Long authorityId);
}
