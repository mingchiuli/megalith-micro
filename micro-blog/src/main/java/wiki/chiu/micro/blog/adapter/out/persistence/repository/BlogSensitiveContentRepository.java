package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;

public interface BlogSensitiveContentRepository
    extends JpaRepository<@NonNull BlogSensitiveContentEntity, @NonNull Long> {

  List<BlogSensitiveContentEntity> findByBlogId(Long blogId);

  List<BlogSensitiveContentEntity> findByBlogIdIn(List<Long> ids);
}
