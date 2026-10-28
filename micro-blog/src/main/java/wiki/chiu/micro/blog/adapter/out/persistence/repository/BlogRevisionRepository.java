package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;

public interface BlogRevisionRepository
    extends JpaRepository<@NonNull BlogRevisionEntity, @NonNull Long> {

    Optional<BlogRevisionEntity> findByIdAndBlogId(Long id, Long blogId);

    Optional<BlogRevisionEntity> findFirstByBlogIdOrderByVersionNoDesc(Long blogId);

    Optional<BlogRevisionEntity> findFirstByBlogIdAndVersionNoLessThanOrderByVersionNoDesc(
        Long blogId, Long versionNo);

    Page<@NonNull BlogRevisionEntity> findByBlogId(Long blogId, Pageable pageable);

    @Query(
        """
        SELECT revision FROM BlogRevisionEntity revision
        WHERE revision.blogId = :blogId
          AND (
            LOWER(revision.title) LIKE LOWER(CONCAT('%', :keywords, '%'))
            OR LOWER(revision.description) LIKE LOWER(CONCAT('%', :keywords, '%'))
            OR LOWER(revision.content) LIKE LOWER(CONCAT('%', :keywords, '%'))
          )
        """)
    Page<@NonNull BlogRevisionEntity> search(
        @Param("blogId") Long blogId, @Param("keywords") String keywords, Pageable pageable);

    List<BlogRevisionEntity> findByBlogIdOrderByVersionNoAsc(Long blogId);

    List<BlogRevisionEntity> findByIdGreaterThanOrderByIdAsc(Long afterId, Pageable pageable);

    long deleteByBlogId(Long blogId);
}
