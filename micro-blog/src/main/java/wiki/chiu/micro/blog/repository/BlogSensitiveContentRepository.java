package wiki.chiu.micro.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;

public interface BlogSensitiveContentRepository extends JpaRepository<BlogSensitiveContentEntity, Long> {

    List<BlogSensitiveContentEntity> findByBlogId(Long blogId);

    List<BlogSensitiveContentEntity> findByBlogIdIn(List<Long> ids);
}
