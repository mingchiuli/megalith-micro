package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.application.model.BlogReadCount;

public interface BlogRepository extends JpaRepository<@NonNull BlogEntity, @NonNull Long> {

    @Query("SELECT blog.id FROM BlogEntity blog WHERE blog.id > :afterId ORDER BY blog.id ASC")
    List<Long> findIdsAfter(@Param("afterId") Long afterId, Pageable pageable);

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

    List<BlogEntity> findByIdGreaterThanOrderByIdAsc(Long afterId, Pageable pageable);

    @Query(
        value = "UPDATE BlogEntity blog SET blog.readCount = blog.readCount + 1 WHERE blog.id = ?1")
    @Modifying
    void setReadCount(Long id);

    @Modifying
    @Query(
        """
        DELETE FROM BlogEntity blog
        WHERE blog.id = :id
          AND COALESCE(blog.publicationRevision, 0) = :publicationRevision
          AND COALESCE(blog.adminRevision, 0) = :adminRevision
        """)
    int deleteByIdAndRevisions(
        @Param("id") Long id,
        @Param("publicationRevision") Long publicationRevision,
        @Param("adminRevision") Long adminRevision);

    List<BlogEntity> findByUserIdIn(List<Long> userIds);

    long countByPublishedRevisionIdIsNotNull();

    @Modifying
    @Query(
        """
        UPDATE BlogEntity blog
        SET blog.adminRevision = COALESCE(blog.adminRevision, 0) + 1
        WHERE blog.id = :id
          AND COALESCE(blog.adminRevision, 0) = :expectedAdminRevision
        """)
    int bumpAdminRevision(
        @Param("id") Long id,
        @Param("expectedAdminRevision") Long expectedAdminRevision);

    @Modifying
    @Query(
        """
        UPDATE BlogEntity blog
        SET blog.publishedRevisionId = :revisionId,
            blog.publishedAt = :publishedAt,
            blog.offlineAt = NULL,
            blog.accessMode = CASE
                WHEN :accessMode IS NULL THEN blog.accessMode
                ELSE :accessMode
            END,
            blog.publicationRevision = COALESCE(blog.publicationRevision, 0) + 1,
            blog.adminRevision = COALESCE(blog.adminRevision, 0) + 1
        WHERE blog.id = :id
          AND COALESCE(blog.publicationRevision, 0) = :expectedPublicationRevision
          AND COALESCE(blog.adminRevision, 0) = :expectedAdminRevision
        """)
    int publish(
        @Param("id") Long id,
        @Param("revisionId") Long revisionId,
        @Param("publishedAt") LocalDateTime publishedAt,
        @Param("accessMode") Integer accessMode,
        @Param("expectedPublicationRevision") Long expectedPublicationRevision,
        @Param("expectedAdminRevision") Long expectedAdminRevision);

    @Modifying
    @Query(
        """
        UPDATE BlogEntity blog
        SET blog.offlineAt = :changedAt,
            blog.publicationRevision = COALESCE(blog.publicationRevision, 0) + 1,
            blog.adminRevision = COALESCE(blog.adminRevision, 0) + 1
        WHERE blog.id = :id
          AND blog.publishedRevisionId IS NOT NULL
          AND COALESCE(blog.publicationRevision, 0) = :expectedPublicationRevision
          AND COALESCE(blog.adminRevision, 0) = :expectedAdminRevision
        """)
    int setOfflineAt(
        @Param("id") Long id,
        @Param("changedAt") LocalDateTime changedAt,
        @Param("expectedPublicationRevision") Long expectedPublicationRevision,
        @Param("expectedAdminRevision") Long expectedAdminRevision);

    @Modifying
    @Query(
        """
        UPDATE BlogEntity blog
        SET blog.offlineAt = NULL,
            blog.publishedAt = :publishedAt,
            blog.publicationRevision = COALESCE(blog.publicationRevision, 0) + 1,
            blog.adminRevision = COALESCE(blog.adminRevision, 0) + 1
        WHERE blog.id = :id
          AND blog.publishedRevisionId IS NOT NULL
          AND COALESCE(blog.publicationRevision, 0) = :expectedPublicationRevision
          AND COALESCE(blog.adminRevision, 0) = :expectedAdminRevision
        """)
    int setOnlineAt(
        @Param("id") Long id,
        @Param("publishedAt") LocalDateTime publishedAt,
        @Param("expectedPublicationRevision") Long expectedPublicationRevision,
        @Param("expectedAdminRevision") Long expectedAdminRevision);
}
