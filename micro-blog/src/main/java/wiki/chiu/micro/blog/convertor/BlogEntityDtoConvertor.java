package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.entity.BlogEntity;

public class BlogEntityDtoConvertor {

    private BlogEntityDtoConvertor() {}

    public static BlogEntityDto convert(BlogEntity blogEntity) {
        return BlogEntityDto.builder()
                .id(blogEntity.getId())
                .title(blogEntity.getTitle())
                .description(blogEntity.getDescription())
                .content(blogEntity.getContent())
                .status(blogEntity.getStatus())
                .link(blogEntity.getLink())
                .userId(blogEntity.getUserId())
                .readCount(blogEntity.getReadCount())
                .updated(blogEntity.getUpdated())
                .created(blogEntity.getCreated())
                .build();
  }

}
