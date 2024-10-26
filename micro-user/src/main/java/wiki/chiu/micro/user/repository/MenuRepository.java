package wiki.chiu.micro.user.repository;


import wiki.chiu.micro.user.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:50 am
 */
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    
    List<MenuEntity> findAllByOrderByOrderNumDesc();

    List<MenuEntity> findByParentId(Long id);

    @Query(value = "SELECT menu.id from MenuEntity menu")
    List<Long> findAllIds();
}
