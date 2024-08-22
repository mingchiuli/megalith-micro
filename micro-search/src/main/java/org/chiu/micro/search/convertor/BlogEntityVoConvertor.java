package org.chiu.micro.search.convertor;

import org.chiu.micro.search.dto.BlogEntityDto;
import org.chiu.micro.search.vo.BlogEntityVo;
import org.chiu.micro.search.page.PageAdapter;
import org.chiu.micro.search.document.BlogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;
import java.util.Map;

public class BlogEntityVoConvertor {

    private BlogEntityVoConvertor() {}

    public static PageAdapter<BlogEntityVo> convert(Page<BlogEntityDto> page, Map<Long, Integer> readMap, Long operateUserId) {
        List<BlogEntityDto> items = page.getContent();

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

    public static PageAdapter<BlogEntityVo> convert(SearchHits<BlogDocument> search, Integer currentPage ,Integer pageSize, Long operateUserId) {
        List<SearchHit<BlogDocument>> hits = search.getSearchHits();
        long totalHits = search.getTotalHits();
        long totalPage = totalHits % pageSize == 0 ? totalHits / pageSize : totalHits / pageSize + 1;

        List<BlogEntityVo> entities = hits.stream()
                .map(hit -> {
                    BlogDocument document = hit.getContent();
                    return BlogEntityVo.builder()
                            .id(document.getId())
                            .title(document.getTitle())
                            .description(document.getDescription())
                            .content(document.getContent())
                            .created(document.getCreated().toLocalDateTime())
                            .updated(document.getUpdated().toLocalDateTime())
                            .status(document.getStatus())
                            .owner(document.getUserId().equals(operateUserId))
                            .build();
                })
                .toList();

        return PageAdapter.<BlogEntityVo>builder()
                .totalElements(totalHits)
                .pageNumber(currentPage)
                .pageSize(pageSize)
                .empty(totalHits == 0)
                .first(currentPage == 1)
                .last(currentPage == totalPage)
                .totalPages((int) totalPage)
                .content(entities)
                .build();
    }
}