package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;

public interface BlogRevisionSensitiveContentRepository
    extends JpaRepository<@NonNull BlogRevisionSensitiveContentEntity, @NonNull Long> {

    List<BlogRevisionSensitiveContentEntity> findByRevisionId(Long revisionId);

    List<BlogRevisionSensitiveContentEntity> findByRevisionIdIn(Collection<Long> revisionIds);

    void deleteByRevisionId(Long revisionId);

    @Modifying
    @Query(
        """
        DELETE FROM BlogRevisionSensitiveContentEntity span
        WHERE span.revisionId IN (
          SELECT revision.id FROM BlogRevisionEntity revision WHERE revision.blogId = :blogId
        )
        """)
    int deleteByBlogId(@Param("blogId") Long blogId);
}
