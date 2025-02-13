package wiki.chiu.micro.blog.convertor;

import java.util.*;

import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.common.vo.SensitiveContentRpcVo;

public class BlogSensitiveContentRpcVoConvertor {

    private BlogSensitiveContentRpcVoConvertor() {}

    public static BlogSensitiveContentRpcVo convert(List<BlogSensitiveContentEntity> entities) {
        if (!entities.isEmpty()) {
            return BlogSensitiveContentRpcVo.builder()
                    .blogId(entities.getFirst().getBlogId())
                    .sensitiveContent(entities.stream()
                            .map(item -> SensitiveContentRpcVo.builder()
                                    .type(item.getType())
                                    .startIndex(item.getStartIndex())
                                    .endIndex(item.getEndIndex())
                                    .build())
                            .toList())
                    .build();
        }
        return BlogSensitiveContentRpcVo.builder().sensitiveContent(Collections.emptyList()).build();
    }
}
