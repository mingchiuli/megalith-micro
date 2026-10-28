package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;

public interface BlogDraftRepository
    extends JpaRepository<@NonNull BlogDraftEntity, @NonNull Long> {

    Optional<BlogDraftEntity> findByBlogId(Long blogId);

    List<BlogDraftEntity> findByBlogIdIn(List<Long> blogIds);

    void deleteByBlogId(Long blogId);
}
