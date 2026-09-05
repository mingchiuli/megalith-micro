package wiki.chiu.micro.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogQueryStore {

    List<Long> findIdsAfter(Long afterId, int limit);

    List<BlogReadCount> findReadCountsAfter(long afterId, int limit);

    List<BlogEntity> findSnapshotsAfter(long afterId, int limit);

    Optional<BlogEntity> findById(Long blogId);

    List<BlogEntity> findAllById(List<Long> blogIds);

    List<BlogEntity> findByUserIds(List<Long> userIds);

    long count();

    PageAdapter<BlogEntity> findPage(int pageNumber, int pageSize, List<Integer> statuses);

    List<BlogSensitiveContentEntity> findSensitiveByBlogId(Long blogId);

    List<BlogSensitiveContentEntity> findSensitiveByBlogIds(List<Long> blogIds);
}
