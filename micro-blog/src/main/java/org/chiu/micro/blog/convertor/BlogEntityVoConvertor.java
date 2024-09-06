package org.chiu.micro.blog.convertor;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.vo.BlogEntityVo;
import org.chiu.micro.blog.page.PageAdapter;

import java.util.List;
import java.util.Map;

public class BlogEntityVoConvertor {

    private BlogEntityVoConvertor() {
    }

    public static PageAdapter<BlogEntityVo> convert(List<BlogEntity> items, Map<Long, Integer> readMap, Long operateUserId, Integer currentPage, Integer size, Long total) {

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

        long anchor = (currentPage - 1) * size + items.size();
        return PageAdapter.<BlogEntityVo>builder()
                .content(entities)
                .last(anchor >= total)
                .first(currentPage == 1)
                .pageNumber(currentPage)
                .totalPages((int) (total % size == 0 ? total / size : total / size + 1))
                .pageSize(size)
                .totalElements(total)
                .empty(items.size() == 0)
                .build();
    }
}