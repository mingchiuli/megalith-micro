package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.domain.MenuEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:50 am
 */
public interface MenuRepository extends JpaRepository<MenuEntity, Long>, MenuReader {

    List<MenuEntity> findAllByOrderByOrderNumDesc();

    List<MenuEntity> findByParentId(Long id);

    boolean existsByParentId(Long id);

    @Query(value = "SELECT menu.id from MenuEntity menu")
    List<Long> findAllIds();
}
