package wiki.chiu.micro.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;

public interface MenuAuthorityRepository extends JpaRepository<MenuAuthorityEntity, Long> {

  @Transactional
  void deleteByMenuId(Long menuId);

  List<MenuAuthorityEntity> findByMenuId(Long menuId);

  @Transactional
  void deleteByAuthorityIdIn(List<Long> ids);

  @Transactional
  void deleteByAuthorityId(Long authorityId);
}
