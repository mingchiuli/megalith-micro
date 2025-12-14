package wiki.chiu.micro.blog.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import wiki.chiu.micro.blog.entity.BlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-27 1:30 am
 */
public interface BlogRepository extends JpaRepository<@NonNull BlogEntity, @NonNull Long> {

    Long countByCreatedGreaterThanEqual(LocalDateTime created);

    @Query(value = "UPDATE BlogEntity blog SET blog.readCount = blog.readCount + 1 WHERE blog.id = ?1")
    @Modifying
    @Transactional
    void setReadCount(Long id);

    @Query(value = "SELECT blog.userId from BlogEntity blog where blog.id = ?1")
    Long findUserIdById(Long id);

    Page<@NonNull BlogEntity> findByStatusIn(PageRequest pageRequest, List<Integer> status);
}
