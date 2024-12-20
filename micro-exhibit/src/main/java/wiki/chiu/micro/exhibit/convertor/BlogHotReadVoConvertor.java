package wiki.chiu.micro.exhibit.convertor;

import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import org.redisson.client.protocol.ScoredEntry;

import java.util.*;
import java.util.stream.Collectors;

import static wiki.chiu.micro.common.lang.BlogStatusEnum.HIDE;


public class BlogHotReadVoConvertor {

    private BlogHotReadVoConvertor() {}

    private static final String UNKNOWN_TITLE = "未知标题";

    public static List<BlogHotReadVo> convert(List<BlogEntityRpcDto> blogs, Collection<ScoredEntry<String>> scoredEntries) {
        Map<Long, String> idTitleMap = createIdTitleMap(blogs);
        List<Long> ids = filterVisibleBlogIds(blogs);

        return scoredEntries.stream()
                .filter(item -> ids.contains(Long.valueOf(item.getValue())))
                .map(item -> createBlogHotReadVo(item, idTitleMap))
                .toList();
    }

    private static Map<Long, String> createIdTitleMap(List<BlogEntityRpcDto> blogs) {
        return blogs.stream()
                .collect(Collectors.toMap(BlogEntityRpcDto::id, BlogEntityRpcDto::title));
    }

    private static List<Long> filterVisibleBlogIds(List<BlogEntityRpcDto> blogs) {
        return blogs.stream()
                .filter(item -> !HIDE.getCode().equals(item.status()))
                .map(BlogEntityRpcDto::id)
                .toList();
    }

    private static BlogHotReadVo createBlogHotReadVo(ScoredEntry<String> item, Map<Long, String> idTitleMap) {
        Long id = Long.valueOf(item.getValue());
        Long readCount = Optional.ofNullable(item.getScore()).orElse(0d).longValue();
        String title = idTitleMap.getOrDefault(id, UNKNOWN_TITLE);

        return BlogHotReadVo.builder()
                .id(id)
                .readCount(readCount)
                .title(title)
                .build();
    }
}
