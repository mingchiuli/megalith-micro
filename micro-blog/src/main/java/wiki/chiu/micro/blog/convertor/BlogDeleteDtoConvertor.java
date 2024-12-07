package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.entity.BlogEntity;

public class BlogDeleteDtoConvertor {

    private BlogDeleteDtoConvertor() {}

    public static BlogDeleteDto convert(BlogEntity blogEntity) {
        return BlogDeleteDto.builder()
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
