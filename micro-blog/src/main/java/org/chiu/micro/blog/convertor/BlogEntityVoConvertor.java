package org.chiu.micro.blog.convertor;

import org.chiu.micro.blog.dto.BlogSearchDto;
import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import org.chiu.micro.blog.vo.BlogEntityVo;
import org.chiu.micro.blog.page.PageAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlogEntityVoConvertor {

    private BlogEntityVoConvertor() {
    }

    public static PageAdapter<BlogEntityVo> convert(List<BlogEntity> items, Map<Long, Integer> readMap, Long operateUserId, List<BlogSensitiveContentEntity> blogSensitiveContentEntities, BlogSearchDto dto) {
        
        Integer size = dto.size();
        Integer currentPage = dto.currentPage();
        Long total = dto.total();
        
        Map<Long, LocalDateTime> blogDate = items.stream()
                .collect(Collectors.toMap(BlogEntity::getId, BlogEntity::getUpdated));
        
        Map<Long, LocalDateTime> blogSensitiveDate = blogSensitiveContentEntities.stream()
                .collect(Collectors.toMap(BlogSensitiveContentEntity::getBlogId, BlogSensitiveContentEntity::getUpdated, (v1, v2) -> v1.isAfter(v2) ? v1 : v2));

        Map<Long, LocalDateTime> mergedMap = Stream.of(blogSensitiveDate, blogDate)
                .flatMap(map -> map.entrySet().stream())
                .collect(HashMap::new, (m, e) -> m.merge(e.getKey(), e.getValue(), (v1, v2) -> v1.isAfter(v2) ? v1 : v2), HashMap::putAll);

        List<BlogEntityVo> entities = items.stream()
                .map(blogEntity -> BlogEntityVo.builder()
                        .id(blogEntity.getId())
                        .title(blogEntity.getTitle())
                        .description(blogEntity.getDescription())
                        .readCount(blogEntity.getReadCount())
                        .recentReadCount(readMap.get(blogEntity.getId()))
                        .status(blogEntity.getStatus())
                        .link(blogEntity.getLink())
                        .created(blogEntity.getCreated())
                        .updated(mergedMap.get(blogEntity.getId()))
                        .content(blogEntity.getContent())
                        .owner(blogEntity.getUserId().equals(operateUserId))
                        .build())
                .toList();

        long anchor = (long) (currentPage - 1) * size + items.size();
        return PageAdapter.<BlogEntityVo>builder()
                .content(entities)
                .last(anchor >= total)
                .first(currentPage == 1)
                .pageNumber(currentPage)
                .totalPages((int) (total % size == 0 ? total / size : total / size + 1))
                .pageSize(size)
                .totalElements(total)
                .empty(items.isEmpty())
                .build();
    }
}
