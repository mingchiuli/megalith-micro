package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.user.domain.Menu;

public interface MenuReader {

    Optional<Menu> findById(Long id);

    List<Menu> findAll();

    List<Menu> findAllById(Iterable<Long> ids);

    List<Menu> findAllByOrderByOrderNumDesc();

    List<Menu> findByParentId(Long parentId);

    boolean existsByParentId(Long parentId);
}
