package org.chiu.micro.blog.convertor;

import java.util.*;

import org.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import org.chiu.micro.blog.vo.SensitiveContentVo;
import org.chiu.micro.blog.vo.BlogSensitiveContentVo;

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
