package wiki.chiu.micro.blog.adapter.in.messaging;

import wiki.chiu.micro.blog.application.model.RecycleBlogCommand;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;

public final class BlogMessageConvertor {

    private BlogMessageConvertor() {
    }

    public static RecycleBlogCommand toCommand(BlogChangedMessage event) {
        BlogOperateEnum operation = BlogOperateEnum.of(event.operation());
        var snapshot = event.blogSnapshot();
        Blog blog = snapshot == null ? null : new Blog(
            snapshot.id(), snapshot.userId(), snapshot.title(), snapshot.description(), snapshot.content(),
            snapshot.created(), snapshot.updated(), snapshot.status(), snapshot.link(), snapshot.readCount(),
            snapshot.revision(), event.adminRevision(), snapshot.sensitiveContent());
        return new RecycleBlogCommand(event.eventId(), operation, event.operatorUserId(), blog);
    }
}
