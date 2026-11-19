package wiki.chiu.micro.search.adapter.in.messaging;

import java.util.List;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

public final class BlogMessageConvertor {

    private BlogMessageConvertor() {
    }

    public static BlogIndexChange toCommand(BlogChangedMessage message) {
        BlogOperateEnum operation = BlogOperateEnum.of(message.operation());
        BlogIndexChange.Operation indexOperation =
            switch (operation) {
                case CREATE -> BlogIndexChange.Operation.CREATE;
                case UPDATE -> BlogIndexChange.Operation.UPDATE;
                case REMOVE -> BlogIndexChange.Operation.REMOVE;
            };
        BlogSnapshot blog = message.blogSnapshot();
        return new BlogIndexChange(
            indexOperation,
            message.revision(),
            new BlogIndexEntry(
                blog.id(),
                blog.userId(),
                blog.status(),
                blog.readCount(),
                blog.title(),
                blog.description(),
                blog.content(),
                blog.link(),
                blog.created(),
                blog.updated(),
                blog.publishedRevisionId(),
                null,
                blog.publishedAt(),
                blog.offlineAt(),
                blog.accessMode(),
                BlogRenderMode.of(blog.renderMode()),
                blog.sensitiveContent() == null ? List.of() : blog.sensitiveContent()));
    }

    public static BlogAdminIndexChange toCommand(BlogAdminIndexMessage message) {
        return new BlogAdminIndexChange(message.operation(), message.blog(), message.draft(),
            message.revision(), message.publishedRevision());
    }
}
