package org.chiu.micro.exhibit.utils;

import java.util.List;

import org.chiu.micro.exhibit.convertor.BlogExhibitDtoConvertor;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.SensitiveContent;
import org.chiu.micro.exhibit.lang.SensitiveTypeEnum;

public class SensitiveUtils {

    private SensitiveUtils() {
    }

    public static BlogExhibitDto deal(List<SensitiveContent> sensitiveWords, BlogExhibitDto blog) {
        List<SensitiveContent> titleSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.TITLE.getCode().equals(item.getType()))
                .toList();

        List<SensitiveContent> descSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.DESCRIPTION.getCode().equals(item.getType()))
                .toList();

        List<SensitiveContent> contentSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.CONTENT.getCode().equals(item.getType()))
                .toList();

        String title = blog.getTitle();
        String description = blog.getDescription();
        String content = blog.getContent();

        for (SensitiveContent item : titleSensitiveList) {
            Integer startIndex = item.getStartIndex();
            Integer endIndex = item.getEndIndex();
            title = title.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    title.substring(endIndex);
        }

        for (SensitiveContent item : descSensitiveList) {
            Integer startIndex = item.getStartIndex();
            Integer endIndex = item.getEndIndex();
            description = description.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    description.substring(endIndex);
        }

        for (SensitiveContent item : contentSensitiveList) {
            Integer startIndex = item.getStartIndex();
            Integer endIndex = item.getEndIndex();
            content = content.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    content.substring(endIndex);
        }

        return BlogExhibitDtoConvertor.convert(blog, title, description, content);
    }

    public static BlogDescriptionDto deal(List<SensitiveContent> sensitiveWords, BlogDescriptionDto blog) {
        List<SensitiveContent> titleSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.TITLE.getCode().equals(item.getType()))
                .toList();

        List<SensitiveContent> descSensitiveList = sensitiveWords.stream()
                .filter(item -> SensitiveTypeEnum.DESCRIPTION.getCode().equals(item.getType()))
                .toList();

        String title = blog.getTitle();
        String description = blog.getDescription();

        for (SensitiveContent item : titleSensitiveList) {
            Integer startIndex = item.getStartIndex();
            Integer endIndex = item.getEndIndex();
            title = title.substring(0, startIndex) +
                    getStar(endIndex - startIndex) +
                    title.substring(endIndex);
        }

        for (SensitiveContent item : descSensitiveList) {
            Integer startIndex = item.getStartIndex();
            Integer endIndex = item.getEndIndex();
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
