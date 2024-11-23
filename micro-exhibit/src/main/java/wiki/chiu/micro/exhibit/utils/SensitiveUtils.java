package wiki.chiu.micro.exhibit.utils;

import java.util.List;

import wiki.chiu.micro.common.dto.SensitiveContentRpcDto;
import wiki.chiu.micro.common.lang.SensitiveTypeEnum;
import wiki.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;

public class SensitiveUtils {

    private SensitiveUtils() {
    }

    public static BlogExhibitDto deal(List<SensitiveContentRpcDto> sensitiveWords, BlogExhibitDto blog) {
        List<SensitiveContentRpcDto> titleSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.TITLE.getCode().equals(item.type()))
                .toList();

        List<SensitiveContentRpcDto> descSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.DESCRIPTION.getCode().equals(item.type()))
                .toList();

        List<SensitiveContentRpcDto> contentSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.CONTENT.getCode().equals(item.type()))
                .toList();

        String title = blog.title();
        String description = blog.description();
        String content = blog.content();

        for (SensitiveContentRpcDto item : titleSensitiveList) {
            Integer startIndex = item.startIndex();
            Integer endIndex = item.endIndex();
            title = title.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    title.substring(endIndex);
        }

        for (SensitiveContentRpcDto item : descSensitiveList) {
            Integer startIndex = item.startIndex();
            Integer endIndex = item.endIndex();
            description = description.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    description.substring(endIndex);
        }

        for (SensitiveContentRpcDto item : contentSensitiveList) {
            Integer startIndex = item.startIndex();
            Integer endIndex = item.endIndex();
            content = content.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    content.substring(endIndex);
        }

        return BlogExhibitDtoConvertor.convert(blog, title, description, content);
    }

    public static BlogDescriptionDto deal(List<SensitiveContentRpcDto> sensitiveWords, BlogDescriptionDto blog) {
        List<SensitiveContentRpcDto> titleSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.TITLE.getCode().equals(item.type()))
                .toList();

        List<SensitiveContentRpcDto> descSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.DESCRIPTION.getCode().equals(item.type()))
                .toList();

        String title = blog.title();
        String description = blog.description();

        for (SensitiveContentRpcDto item : titleSensitiveList) {
            Integer startIndex = item.startIndex();
            Integer endIndex = item.endIndex();
            title = title.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    title.substring(endIndex);
        }

        for (SensitiveContentRpcDto item : descSensitiveList) {
            Integer startIndex = item.startIndex();
            Integer endIndex = item.endIndex();
            description = description.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    description.substring(endIndex);
        }

        return BlogExhibitDtoConvertor.convert(blog, title, description);

    }

    private static String getStar(Integer length) {
        return "+".repeat(length);
    }
}
