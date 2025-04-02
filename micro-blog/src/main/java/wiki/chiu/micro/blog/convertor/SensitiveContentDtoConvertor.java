package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.dto.BlogPushAllDto;
import wiki.chiu.micro.blog.req.SensitiveContentReq;

import java.util.List;

public class SensitiveContentDtoConvertor {
    public static List<BlogPushAllDto.SensitiveContentDto> convert(List<SensitiveContentReq> sensitiveContentReqs) {
        return sensitiveContentReqs.stream()
                .map(req -> BlogPushAllDto.SensitiveContentDto.builder()
                        .type(req.type())
                        .startIndex(req.startIndex())
                        .endIndex(req.endIndex())
                        .build())
                .toList();
    }
}
