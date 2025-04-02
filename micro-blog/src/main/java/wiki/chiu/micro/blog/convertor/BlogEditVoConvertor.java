package wiki.chiu.micro.blog.convertor;

import java.util.List;

import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.vo.BlogEditVo;

public class BlogEditVoConvertor {

    private BlogEditVoConvertor() {}

    public static BlogEditVo convert(BlogEntityDto blog, List<BlogEditVo.SensitiveContentVo> sensitiveContentList) {
        return BlogEditVo.builder()
                .id(blog.id())
                .title(blog.title())
                .description(blog.description())
                .content(blog.content())
                .link(blog.link())
                .status(blog.status())
                .sensitiveContentList(sensitiveContentList.stream()
                        .map(item -> BlogEditVo.SensitiveContentVo.builder()
                                .type(item.type())
                                .startIndex(item.startIndex())
                                .endIndex(item.endIndex())
                                .build())
                        .toList())
                .build();

    }
}
