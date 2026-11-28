package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.common.enums.BlogRenderMode;

public interface BlogDraftRepository
    extends JpaRepository<@NonNull BlogDraftEntity, @NonNull Long> {

    Optional<BlogDraftEntity> findByBlogId(Long blogId);

    List<BlogDraftEntity> findByBlogIdIn(Collection<Long> blogIds);

    @Modifying
    @Query("DELETE FROM BlogDraftEntity entity WHERE entity.blogId = :blogId")
    void deleteByBlogId(@Param("blogId") Long blogId);

    @Modifying
    @Query(
        """
        UPDATE BlogDraftEntity draft
        SET draft.basePublishedRevisionId = :baseRevisionId,
            draft.title = :title,
            draft.description = :description,
            draft.content = :content,
            draft.link = :link,
            draft.renderMode = :renderMode,
            draft.updatedBy = :userId,
            draft.updatedAt = :updatedAt,
            draft.draftRevision = draft.draftRevision + 1
        WHERE draft.id = :id
          AND draft.draftRevision = :expectedRevision
        """)
    int updateContent(
        @Param("id") Long id,
        @Param("expectedRevision") Long expectedRevision,
        @Param("baseRevisionId") Long baseRevisionId,
        @Param("title") String title,
        @Param("description") String description,
        @Param("content") String content,
        @Param("link") String link,
        @Param("renderMode") BlogRenderMode renderMode,
        @Param("userId") Long userId,
        @Param("updatedAt") LocalDateTime updatedAt);
}
