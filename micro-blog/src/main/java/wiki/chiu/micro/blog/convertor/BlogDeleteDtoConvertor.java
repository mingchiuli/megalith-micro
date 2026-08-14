package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.common.lang.BlogSnapshot;

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
        .eventRevision(blogEntity.getEventRevision())
        .updated(blogEntity.getUpdated())
        .created(blogEntity.getCreated())
        .build();
  }

  public static BlogDeleteDto convert(BlogSnapshot snapshot) {
    return BlogDeleteDto.builder()
        .id(snapshot.id())
        .title(snapshot.title())
        .description(snapshot.description())
        .content(snapshot.content())
        .status(snapshot.status())
        .link(snapshot.link())
        .userId(snapshot.userId())
        .readCount(snapshot.readCount())
        .eventRevision(snapshot.revision())
        .updated(snapshot.updated())
        .created(snapshot.created())
        .build();
  }
}
