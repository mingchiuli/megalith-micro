package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;

public interface BlogRevisionRepository
    extends JpaRepository<@NonNull BlogRevisionEntity, @NonNull Long> {

    Optional<BlogRevisionEntity> findByIdAndBlogId(Long id, Long blogId);

    Optional<BlogRevisionEntity> findFirstByBlogIdOrderByVersionNoDesc(Long blogId);

    List<BlogRevisionEntity> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    Optional<BlogRevisionEntity> findFirstByBlogIdAndVersionNoLessThanOrderByVersionNoDesc(
        Long blogId, Long versionNo);

    List<BlogRevisionEntity> findByBlogIdOrderByVersionNoAsc(Long blogId);

    long deleteByBlogId(Long blogId);

    @Modifying
    @Query(
        """
        UPDATE BlogRevisionEntity revision
        SET revision.firstPublishedAt = :publishedAt
        WHERE revision.id = :id
          AND revision.blogId = :blogId
          AND revision.firstPublishedAt IS NULL
        """)
    int markFirstPublished(
        @Param("id") Long id,
        @Param("blogId") Long blogId,
        @Param("publishedAt") LocalDateTime publishedAt);
}
