package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;

public interface MenuAuthorityRepository
    extends JpaRepository<MenuAuthorityEntity, Long>, MenuAuthorityReader {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM MenuAuthorityEntity menuAuthority WHERE menuAuthority.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);

    List<MenuAuthorityEntity> findByMenuId(Long menuId);

    List<MenuAuthorityEntity> findByMenuIdIn(List<Long> menuIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        "DELETE FROM MenuAuthorityEntity menuAuthority WHERE menuAuthority.authorityId IN"
            + " :authorityIds")
    void deleteByAuthorityIdIn(@Param("authorityIds") List<Long> authorityIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        "DELETE FROM MenuAuthorityEntity menuAuthority WHERE menuAuthority.authorityId ="
            + " :authorityId")
    void deleteByAuthorityId(@Param("authorityId") Long authorityId);
}
