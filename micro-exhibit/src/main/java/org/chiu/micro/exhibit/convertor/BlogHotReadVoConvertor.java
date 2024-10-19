package org.chiu.micro.exhibit.convertor;

import org.chiu.micro.common.dto.BlogEntityRpcDto;
import org.chiu.micro.exhibit.vo.BlogHotReadVo;
import org.redisson.client.protocol.ScoredEntry;

import java.util.*;
import java.util.stream.Collectors;

import static org.chiu.micro.common.lang.StatusEnum.HIDE;


public class BlogHotReadVoConvertor {

    private BlogHotReadVoConvertor() {}

    public static List<BlogHotReadVo> convert(List<BlogEntityRpcDto> blogs, Collection<ScoredEntry<String>> scoredEntries) {
    
        Map<Long, String> idTitleMap = blogs.stream()
                .collect(Collectors.toMap(BlogEntityRpcDto::id, BlogEntityRpcDto::title));

        List<Long> ids = blogs.stream()
                .filter(item -> !HIDE.getCode().equals(item.status()))
                .map(BlogEntityRpcDto::id)
                .toList();

        return scoredEntries.stream()
                .filter(item -> ids.contains(Long.valueOf(item.getValue())))
                .map(item -> BlogHotReadVo.builder()
                        .id(Long.valueOf(item.getValue()))
                        .readCount(Optional.ofNullable(item.getScore()).orElse(0d).longValue())
                        .title(idTitleMap.getOrDefault(Long.valueOf(item.getValue()), "未知标题"))
                        .build())
                .toList();
    }
}
