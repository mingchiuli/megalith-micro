package org.chiu.micro.blog.convertor;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.vo.BlogDeleteVo;
import org.chiu.micro.common.page.PageAdapter;

import java.util.ArrayList;
import java.util.List;

public class  BlogDeleteVoConvertor {

    private BlogDeleteVoConvertor() {}

    public static PageAdapter<BlogDeleteVo> convert(Integer index, List<BlogEntity> entities, Integer currentPage, Integer size, Long total) {

        int totalPages = (int) (total % size == 0 ? total / size : total / size + 1);

        List<BlogDeleteVo> content = new ArrayList<>();
        for (BlogEntity item : entities) {
            content.add(BlogDeleteVo.builder()
                    .idx(index++)
                    .link(item.getLink())
                    .content(item.getContent())
                    .readCount(item.getReadCount())
                    .title(item.getTitle())
                    .status(item.getStatus())
                    .created(item.getCreated())
                    .updated(item.getUpdated())
                    .id(item.getId())
                    .userId(item.getUserId())
                    .description(item.getDescription())
                    .build());
        }

        return PageAdapter.<BlogDeleteVo>builder()
                .content(content)
                .last(currentPage == totalPages)
                .first(currentPage == 1)
                .pageNumber(currentPage)
                .totalPages(totalPages)
                .pageSize(size)
                .totalElements(total)
                .empty(total == 0)
                .build();
    }
}
