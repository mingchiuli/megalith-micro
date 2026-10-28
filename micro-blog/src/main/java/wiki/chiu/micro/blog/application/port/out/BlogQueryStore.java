package wiki.chiu.micro.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;

public interface BlogQueryStore {

    List<Long> findIdsAfter(Long afterId, int limit);

    List<BlogReadCount> findReadCountsAfter(long afterId, int limit);

    List<BlogSnapshot> findPublishedSnapshotsAfter(long afterId, int limit);

    Optional<Blog> findById(Long blogId);

    List<BlogDeletionTarget> findDeletionTargetsByIds(List<Long> blogIds);

    List<BlogDeletionTarget> findDeletionTargetsByUserIds(List<Long> userIds);

    long count();

    List<BlogSensitiveSpan> findSensitiveByBlogId(Long blogId);

}
