package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.req.BlogEntityReq;

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

    public static void convert(BlogEntityReq blog, BlogEntity blogEntity) {
        blogEntity.setTitle(blog.title());
        blogEntity.setDescription(blog.description());
        blogEntity.setContent(blog.content());
        blogEntity.setId(blog.id().orElse(null));
        blogEntity.setStatus(blog.status());
        blogEntity.setLink(blog.link());
    }
}
