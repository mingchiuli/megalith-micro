package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;

public interface BlogDraftSensitiveContentRepository
    extends JpaRepository<@NonNull BlogDraftSensitiveContentEntity, @NonNull Long> {

    List<BlogDraftSensitiveContentEntity> findByBlogId(Long blogId);

    void deleteByBlogId(Long blogId);
}
