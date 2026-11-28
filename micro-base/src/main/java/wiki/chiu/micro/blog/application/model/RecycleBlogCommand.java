package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogOperateEnum;

public record RecycleBlogCommand(
    String eventId, BlogOperateEnum operation, Long operatorUserId, Blog blog) {
}
