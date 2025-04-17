package wiki.chiu.micro.blog.convertor;

import java.util.List;
import java.util.Objects;

import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.vo.BlogEditVo;

public class BlogEditVoConvertor {

    private BlogEditVoConvertor() {}

    public static BlogEditVo convert(BlogEntity blog, List<BlogEditVo.SensitiveContentVo> sensitiveContentList, Long userId, Long blogUserId, List<String> roles, String highestRole) {
        return BlogEditVo.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .link(blog.getLink())
                .status(blog.getStatus())
                .owner(Objects.equals(userId, blogUserId) || roles.contains(highestRole))
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
