package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.common.lang.BlogSnapshot;

public final class BlogSnapshotConvertor {

    private BlogSnapshotConvertor() {
    }

    public static BlogSnapshot convert(BlogEntity blog) {
        return new BlogSnapshot(
            blog.getId(), blog.getUserId(), blog.getTitle(), blog.getDescription(),
            blog.getContent(), blog.getCreated(), blog.getUpdated(), blog.getStatus(),
            blog.getLink(), blog.getReadCount(), blog.getEventRevision());
    }
}
