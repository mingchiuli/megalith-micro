package wiki.chiu.micro.exhibit.utils;

import java.util.List;

import wiki.chiu.micro.common.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.common.lang.SensitiveTypeEnum;
import wiki.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;

public class SensitiveUtils {

    private SensitiveUtils() {
    }

    private static final String STAR = "+";

    public static BlogExhibitDto deal(List<SensitiveContentRpcVo> sensitiveWords, BlogExhibitDto blog) {
        String title = processSensitiveContent(sensitiveWords, blog.title(), SensitiveTypeEnum.TITLE);
        String description = processSensitiveContent(sensitiveWords, blog.description(), SensitiveTypeEnum.DESCRIPTION);
        String content = processSensitiveContent(sensitiveWords, blog.content(), SensitiveTypeEnum.CONTENT);

        return BlogExhibitDtoConvertor.convert(blog, title, description, content);
    }

    public static BlogDescriptionDto deal(List<SensitiveContentRpcVo> sensitiveWords, BlogDescriptionDto blog) {
        String title = processSensitiveContent(sensitiveWords, blog.title(), SensitiveTypeEnum.TITLE);
        String description = processSensitiveContent(sensitiveWords, blog.description(), SensitiveTypeEnum.DESCRIPTION);

        return BlogExhibitDtoConvertor.convert(blog, title, description);
    }

    private static String processSensitiveContent(List<SensitiveContentRpcVo> sensitiveWords, String content, SensitiveTypeEnum type) {
        List<SensitiveContentRpcVo> filteredWords = sensitiveWords.stream()
                .filter(item -> type.getCode().equals(item.type()))
                .toList();

        for (SensitiveContentRpcVo item : filteredWords) {
            Integer startIndex = item.startIndex();
            Integer endIndex = item.endIndex();
            content = content.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    content.substring(endIndex);
        }

        return content;
    }

    private static String getStar(Integer length) {
        return STAR.repeat(length);
    }
}
