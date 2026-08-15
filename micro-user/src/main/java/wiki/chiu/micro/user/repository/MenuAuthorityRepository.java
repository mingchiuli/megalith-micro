package wiki.chiu.micro.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

public interface MenuAuthorityRepository extends JpaRepository<MenuAuthorityEntity, Long> {

  void deleteByMenuId(Long menuId);

  List<MenuAuthorityEntity> findByMenuId(Long menuId);

  List<MenuAuthorityEntity> findByMenuIdIn(List<Long> menuIds);

  void deleteByAuthorityIdIn(List<Long> ids);

  void deleteByAuthorityId(Long authorityId);
}
