package wiki.chiu.micro.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

import java.util.List;

public interface MenuAuthorityRepository extends JpaRepository<MenuAuthorityEntity, Long> {

    @Query(value = "DELETE from MenuAuthorityEntity menuAuthority where menuAuthority.menuId in (?1)")
    @Modifying
    @Transactional
    void deleteAllByMenuId(List<Long> ids);

    void deleteByMenuId(Long menuId);

    List<MenuAuthorityEntity> findByMenuId(Long menuId);
}
