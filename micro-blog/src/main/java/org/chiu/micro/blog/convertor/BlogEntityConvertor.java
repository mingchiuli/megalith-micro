package org.chiu.micro.blog.convertor;

import org.chiu.micro.blog.dto.BlogEntityDto;
import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.req.BlogEntityReq;
import org.springframework.util.StringUtils;

import java.util.Map;

import static org.chiu.micro.blog.lang.MessageActionFieldEnum.*;

public class BlogEntityConvertor {

    private BlogEntityConvertor() {}

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

    public static void convert(BlogEntityReq blog, BlogEntity blogEntity) {
        blogEntity.setTitle(blog.title());
        blogEntity.setDescription(blog.description());
        blogEntity.setContent(blog.content());
        blogEntity.setId(blog.id());
        blogEntity.setStatus(blog.status());
        blogEntity.setLink(blog.link());
    }
}
