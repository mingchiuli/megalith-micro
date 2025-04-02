package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogPushAllDto;
import wiki.chiu.micro.blog.req.BlogEditPushAllReq;

public class BlogPushAllDtoConvertor {

    public static BlogPushAllDto convert(BlogEditPushAllReq blog) {
        return BlogPushAllDto.builder()
                .id(blog.id().orElse(null))
                .title(blog.title())
                .description(blog.description())
                .content(blog.content())
                .link(blog.link())
                .status(blog.status())
                .sensitiveContentList(SensitiveContentDtoConvertor.convert(blog.sensitiveContentList()))
                .build();
    }
}
