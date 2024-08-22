package org.chiu.micro.blog.convertor;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.vo.BlogEntityVo;
import org.chiu.micro.blog.page.PageAdapter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public class BlogEntityVoConvertor {

    private BlogEntityVoConvertor() {}

    public static PageAdapter<BlogEntityVo> convert(Page<BlogEntity> page, Map<Long, Integer> readMap, Long operateUserId) {
        List<BlogEntity> items = page.getContent();

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
                        .updated(blogEntity.getUpdated())
                        .content(blogEntity.getContent())
                        .owner(blogEntity.getUserId().equals(operateUserId))
                        .build())
                .toList();

        return PageAdapter.<BlogEntityVo>builder()
                .content(entities)
                .last(page.isLast())
                .first(page.isFirst())
                .pageNumber(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .empty(page.isEmpty())
                .build();
    }
}