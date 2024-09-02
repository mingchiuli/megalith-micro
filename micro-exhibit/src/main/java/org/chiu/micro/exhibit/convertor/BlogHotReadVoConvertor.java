package org.chiu.micro.exhibit.convertor;

import org.chiu.micro.exhibit.vo.BlogHotReadVo;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.chiu.micro.exhibit.lang.StatusEnum.HIDE;

public class BlogHotReadVoConvertor {

    private BlogHotReadVoConvertor() {}

    public static List<BlogHotReadVo> convert(List<BlogEntityDto> blogs, Set<ZSetOperations.TypedTuple<String>> set) {
    
        Map<Long, String> idTitleMap = blogs.stream().collect(Collectors.toMap(BlogEntityDto::getId, BlogEntityDto::getTitle));

        List<Long> ids = blogs.stream()
                .filter(item -> !HIDE.getCode().equals(item.getStatus()))
                .map(BlogEntityDto::getId)
                .toList();

        return Optional.ofNullable(set).orElseGet(LinkedHashSet::new).stream()
                .filter(item -> ids.contains(Long.valueOf(item.getValue())))
                .map(item -> BlogHotReadVo.builder()
                        .id(Long.valueOf(item.getValue()))
                        .readCount(Optional.ofNullable(item.getScore()).orElse(0d).longValue())
                        .title(idTitleMap.get(Long.valueOf(item.getValue())))
                        .build())
                .toList();
    }
}
