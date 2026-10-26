package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.application.model.BlogReadCount;

public interface BlogRepository extends JpaRepository<@NonNull BlogEntity, @NonNull Long> {

    @Query("SELECT blog.id FROM BlogEntity blog WHERE blog.id > :afterId ORDER BY blog.id ASC")
    List<Long> findIdsAfter(@Param("afterId") Long afterId, Pageable pageable);

    @Query("SELECT blog.id FROM BlogEntity blog ORDER BY blog.id ASC")
    List<Long> findAllIds(Pageable pageable);

    @Query(
        """
        SELECT new wiki.chiu.micro.blog.application.model.BlogReadCount(
            blog.id, COALESCE(blog.readCount, 0L))
        FROM BlogEntity blog
        WHERE blog.publishedRevisionId IS NOT NULL AND blog.id > :afterId
        ORDER BY blog.id ASC
        """)
    List<BlogReadCount> findReadCountsAfter(@Param("afterId") long afterId, Pageable pageable);

    List<BlogEntity> findByPublishedRevisionIdIsNotNullAndIdGreaterThanOrderByIdAsc(
        Long afterId, Pageable pageable);

    @Query(
        value = "UPDATE BlogEntity blog SET blog.readCount = blog.readCount + 1 WHERE blog.id = ?1")
    @Modifying
    void setReadCount(Long id);

    @Query("SELECT blog.userId FROM BlogEntity blog WHERE blog.id = ?1")
    Long findUserIdById(Long id);

    List<BlogEntity> findByUserIdIn(List<Long> userIds);

    long countByPublishedRevisionIdIsNotNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT blog FROM BlogEntity blog WHERE blog.id = :id")
    Optional<BlogEntity> findForUpdate(@Param("id") Long id);
}
