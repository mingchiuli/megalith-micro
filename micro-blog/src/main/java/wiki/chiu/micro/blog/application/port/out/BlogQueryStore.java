package wiki.chiu.micro.blog.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogQueryStore {

    Optional<BlogEntity> findById(Long blogId);

    List<BlogEntity> findAllById(List<Long> blogIds);

    List<BlogEntity> findByUserIds(List<Long> userIds);

    long count();

    long countCreatedSince(LocalDateTime created);

    PageAdapter<BlogEntity> findPage(int pageNumber, int pageSize, List<Integer> statuses);

    List<BlogSensitiveContentEntity> findSensitiveByBlogId(Long blogId);

    List<BlogSensitiveContentEntity> findSensitiveByBlogIds(List<Long> blogIds);
}
