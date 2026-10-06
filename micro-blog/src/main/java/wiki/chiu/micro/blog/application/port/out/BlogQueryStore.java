package wiki.chiu.micro.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;

public interface BlogQueryStore {

    List<Long> findIdsAfter(Long afterId, int limit);

    List<BlogReadCount> findReadCountsAfter(long afterId, int limit);

    List<Blog> findSnapshotsAfter(long afterId, int limit);

    Optional<Blog> findById(Long blogId);

    List<Blog> findAllById(List<Long> blogIds);

    List<Blog> findByUserIds(List<Long> userIds);

    long count();

    Page<Blog> findPage(int pageNumber, int pageSize, List<Integer> statuses);

    List<SensitiveContent> findSensitiveByBlogId(Long blogId);

    List<SensitiveContent> findSensitiveByBlogIds(List<Long> blogIds);
}
