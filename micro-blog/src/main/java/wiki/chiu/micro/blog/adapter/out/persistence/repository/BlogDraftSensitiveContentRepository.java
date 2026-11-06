package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;

public interface BlogDraftSensitiveContentRepository
    extends JpaRepository<@NonNull BlogDraftSensitiveContentEntity, @NonNull Long> {

    List<BlogDraftSensitiveContentEntity> findByBlogId(Long blogId);

    List<BlogDraftSensitiveContentEntity> findByBlogIdIn(Collection<Long> blogIds);

    @Modifying
    @Query("DELETE FROM BlogDraftSensitiveContentEntity entity WHERE entity.blogId = :blogId")
    void deleteByBlogId(@Param("blogId") Long blogId);
}
