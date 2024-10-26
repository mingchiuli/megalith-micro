package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import org.springframework.util.StringUtils;

import java.util.Map;

import static wiki.chiu.micro.common.lang.MessageActionFieldEnum.*;

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
        blogEntity.setId(blog.id().orElse(null));
        blogEntity.setStatus(blog.status());
        blogEntity.setLink(blog.link());
    }
}
