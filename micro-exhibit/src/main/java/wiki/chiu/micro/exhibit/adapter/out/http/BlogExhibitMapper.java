package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;

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

    public static BlogExhibit toModel(PublicBlogRpcVo blog, UserEntityRpcVo author) {
        return new BlogExhibit(
            blog.userId(),
            blog.description(),
            author.nickname(),
            author.avatar(),
            blog.title(),
            blog.content(),
            blog.publishedAt() == null ? blog.created() : blog.publishedAt(),
            blog.readCount(),
            BlogHttpMapper.publicStatus(blog),
            blog.sensitiveContent() == null
                ? List.of()
                : blog.sensitiveContent());
    }
}
