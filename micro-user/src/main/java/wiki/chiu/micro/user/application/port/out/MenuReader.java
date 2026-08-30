package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;
import wiki.chiu.micro.user.domain.MenuEntity;

public interface MenuReader {

  Optional<MenuEntity> findById(Long id);

  List<MenuEntity> findAll();

  List<MenuEntity> findAllById(Iterable<Long> ids);

  List<MenuEntity> findAllByOrderByOrderNumDesc();

  List<MenuEntity> findByParentId(Long parentId);

  boolean existsByParentId(Long parentId);

  List<Long> findAllIds();
}
