package wiki.chiu.micro.blog.convertor;

import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.entity.BlogEntity;

import java.util.Map;

import static wiki.chiu.micro.common.lang.MessageActionFieldEnum.*;
import static wiki.chiu.micro.common.lang.MessageActionFieldEnum.LINK;

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

    public static BlogEntityDto convert(Map<String, String> entries) {
        String idStr = entries.get(ID.getMsg());
        return BlogEntityDto.builder()
                .id(StringUtils.hasLength(idStr) ?
                        Long.valueOf(idStr) :
                        null)
                .userId(Long.valueOf(entries.get(USER_ID.getMsg())))
                .description(entries.get(DESCRIPTION.getMsg()))
                .title(entries.get(TITLE.getMsg()))
                .status(Integer.valueOf(entries.get(STATUS.getMsg())))
                .link(entries.get(LINK.getMsg()))
                .build();
    }

}
