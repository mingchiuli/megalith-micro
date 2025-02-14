package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.req.BlogEntityReq;

import static wiki.chiu.micro.common.lang.BlogStatusEnum.HIDE;

public class BlogEntityConvertor {

    private BlogEntityConvertor() {}

   public static BlogEntity convert(BlogDeleteDto blogDeleteDto) {
       return BlogEntity.builder()
               .id(blogDeleteDto.id())
               .title(blogDeleteDto.title())
               .description(blogDeleteDto.description())
               .content(blogDeleteDto.content())
               .status(blogDeleteDto.status())
               .link(blogDeleteDto.link())
               .userId(blogDeleteDto.userId())
               .readCount(blogDeleteDto.readCount())
               .updated(blogDeleteDto.updated())
               .created(blogDeleteDto.created())
               .build();
   }

    public static BlogEntity convertRecover(BlogDeleteDto blogDeleteDto) {
        return BlogEntity.builder()
                .title(blogDeleteDto.title())
                .status(HIDE.getCode())
                .description(blogDeleteDto.description())
                .content(blogDeleteDto.content())
                .status(blogDeleteDto.status())
                .link(blogDeleteDto.link())
                .created(blogDeleteDto.created())
                .userId(blogDeleteDto.userId())
                .readCount(blogDeleteDto.readCount())
                .build();
    }

    public static BlogEntity convert(BlogEntityReq blog, BlogEntity dealBlog) {
        BlogEntity blogEntity = new BlogEntity();
        blogEntity.setTitle(blog.title());
        blogEntity.setDescription(blog.description());
        blogEntity.setContent(blog.content());
        blogEntity.setId(blog.id().orElse(null));
        blogEntity.setStatus(blog.status());
        blogEntity.setLink(blog.link());

        blogEntity.setUserId(dealBlog.getUserId());
        blogEntity.setReadCount(dealBlog.getReadCount());
        blogEntity.setCreated(dealBlog.getCreated());

        return blogEntity;
    }
}
