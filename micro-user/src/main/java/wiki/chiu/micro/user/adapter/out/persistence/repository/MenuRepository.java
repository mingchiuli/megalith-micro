package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.user.adapter.out.persistence.entity.MenuEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:50 am
 */
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {

    List<MenuEntity> findAllByOrderByOrderNumDesc();

    List<MenuEntity> findByParentId(Long id);

    boolean existsByParentId(Long id);
}
