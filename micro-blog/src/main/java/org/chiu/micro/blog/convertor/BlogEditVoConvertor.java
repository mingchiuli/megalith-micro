package org.chiu.micro.blog.convertor;

import java.util.List;

import org.chiu.micro.blog.dto.BlogEntityDto;
import org.chiu.micro.blog.vo.SensitiveContentVo;
import org.chiu.micro.blog.vo.BlogEditVo;

public class BlogEditVoConvertor {

    private BlogEditVoConvertor() {}

    public static BlogEditVo convert(BlogEntityDto blog, Integer version, List<SensitiveContentVo> sensitiveContentList) {
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
