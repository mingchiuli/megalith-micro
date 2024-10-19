package org.chiu.micro.exhibit.convertor;

import org.chiu.micro.common.page.PageAdapter;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.vo.BlogDescriptionVo;

import java.util.List;

public class BlogDescriptionVoConvertor {

    private BlogDescriptionVoConvertor() {
    }

    public static PageAdapter<BlogDescriptionVo> convert(PageAdapter<BlogDescriptionDto> page) {
        List<BlogDescriptionVo> vos = page.content().stream()
                .map(dto -> BlogDescriptionVo.builder()
                        .id(dto.id())
                        .description(dto.description())
                        .title(dto.title())
                        .created(dto.created())
                        .link(dto.link())
                        .build())
                .toList();

        return PageAdapter.<BlogDescriptionVo>builder()
                .content(vos)
                .first(page.first())
                .last(page.last())
                .empty(page.empty())
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .totalElements(page.totalElements())
                .totalPages(page.totalPages())
                .build();
    }
}
