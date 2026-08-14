package wiki.chiu.micro.blog.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import wiki.chiu.micro.blog.entity.BlogEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 1:30 am
 */
public interface BlogRepository extends JpaRepository<@NonNull BlogEntity, @NonNull Long> {

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT blog FROM BlogEntity blog WHERE blog.id = ?1")
  java.util.Optional<BlogEntity> findByIdForUpdate(Long id);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT blog FROM BlogEntity blog WHERE blog.id IN ?1")
  List<BlogEntity> findAllByIdForUpdate(List<Long> ids);

  Long countByCreatedGreaterThanEqual(LocalDateTime created);

  @Query(
      value = "UPDATE BlogEntity blog SET blog.readCount = blog.readCount + 1 WHERE blog.id = ?1")
  @Modifying
  void setReadCount(Long id);

  @Query(value = "SELECT blog.userId from BlogEntity blog where blog.id = ?1")
  Long findUserIdById(Long id);

  Page<@NonNull BlogEntity> findByStatusIn(PageRequest pageRequest, List<Integer> status);
}
