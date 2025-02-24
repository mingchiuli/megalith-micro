package wiki.chiu.micro.blog.convertor;

import java.util.List;

import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.common.vo.SensitiveContentRpcVo;

public class BlogEditVoConvertor {

    private BlogEditVoConvertor() {}

    public static BlogEditVo convert(BlogEntityDto blog, Integer version, List<SensitiveContentRpcVo> sensitiveContentList) {
        return BlogEditVo.builder()
                .userId(blog.userId())
                .id(blog.id())
                .title(blog.title())
                .description(blog.description())
                .content(blog.content())
                .link(blog.link())
                .version(version)
                .status(blog.status())
                .sensitiveContentList(sensitiveContentList)
                .build();
    }
}
