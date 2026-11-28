package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.user.adapter.out.persistence.entity.MenuEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:50 am
 */
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {

    @Modifying
    @Query(
        """
        UPDATE MenuEntity entity
        SET entity.parentId = :parentId,
            entity.title = :title,
            entity.name = :name,
            entity.url = :url,
            entity.component = :component,
            entity.type = :type,
            entity.icon = :icon,
            entity.orderNum = :orderNum,
            entity.status = :status,
            entity.updated = :updated
        WHERE entity.id = :id
        """)
    int updateDetails(
        @Param("id") Long id,
        @Param("parentId") Long parentId,
        @Param("title") String title,
        @Param("name") String name,
        @Param("url") String url,
        @Param("component") String component,
        @Param("type") Integer type,
        @Param("icon") String icon,
        @Param("orderNum") Integer orderNum,
        @Param("status") Integer status,
        @Param("updated") LocalDateTime updated);

    default int updateDetails(MenuEntity entity, LocalDateTime updated) {
        return updateDetails(
            entity.getId(),
            entity.getParentId(),
            entity.getTitle(),
            entity.getName(),
            entity.getUrl(),
            entity.getComponent(),
            entity.getType(),
            entity.getIcon(),
            entity.getOrderNum(),
            entity.getStatus(),
            updated);
    }

    List<MenuEntity> findAllByOrderByOrderNumDesc();

    List<MenuEntity> findByParentId(Long id);

    boolean existsByParentId(Long id);
}
