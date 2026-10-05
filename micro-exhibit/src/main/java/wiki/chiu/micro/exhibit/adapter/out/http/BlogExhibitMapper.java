package wiki.chiu.micro.exhibit.adapter.out.http;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

/**
 * Turns the blog and user service payloads into the exhibit service's own model.
 */
public final class BlogExhibitMapper {

    private BlogExhibitMapper() {
    }

    public static BlogExhibit toModel(BlogEntityRpcVo blog, UserEntityRpcVo author) {
        return new BlogExhibit(
            blog.userId(),
            blog.description(),
            author.nickname(),
            author.avatar(),
            blog.title(),
            blog.content(),
            blog.created(),
            blog.readCount(),
            blog.status());
    }
}
