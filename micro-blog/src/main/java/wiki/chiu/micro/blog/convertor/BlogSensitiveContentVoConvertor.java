package wiki.chiu.micro.blog.convertor;

import java.util.*;

import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.vo.SensitiveContentVo;
import wiki.chiu.micro.blog.vo.BlogSensitiveContentVo;

public class BlogSensitiveContentVoConvertor {

    private BlogSensitiveContentVoConvertor() {}

    public static BlogSensitiveContentVo convert(List<BlogSensitiveContentEntity> entities) {
        if (!entities.isEmpty()) {
            return BlogSensitiveContentVo.builder()
                    .blogId(entities.getFirst().getBlogId())
                    .sensitiveContent(entities.stream()
                            .map(item -> SensitiveContentVo.builder()
                                    .type(item.getType())
                                    .startIndex(item.getStartIndex())
                                    .endIndex(item.getEndIndex())
                                    .build())
                            .toList())
                    .build();
        }
        return BlogSensitiveContentVo.builder().sensitiveContent(Collections.emptyList()).build();
    }
}
