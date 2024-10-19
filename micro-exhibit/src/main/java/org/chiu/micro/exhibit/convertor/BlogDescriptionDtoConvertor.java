package org.chiu.micro.exhibit.convertor;

import java.util.List;

import org.chiu.micro.common.dto.BlogEntityRpcDto;
import org.chiu.micro.common.page.PageAdapter;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;

/**
 * @Author limingjiu
 * @Date 2024/5/10 11:16
 **/
public class BlogDescriptionDtoConvertor {

    private BlogDescriptionDtoConvertor() {
    }

    public static PageAdapter<BlogDescriptionDto> convert(PageAdapter<BlogEntityRpcDto> page) {

        List<BlogDescriptionDto> dtos = page.content().stream()
                .map(item -> BlogDescriptionDto.builder()
                        .id(item.id())
                        .description(item.description())
                        .title(item.title())
                        .status(item.status())
                        .created(item.created())
                        .link(item.link())
                        .build())
                .toList();

        return PageAdapter.<BlogDescriptionDto>builder()
                .content(dtos)
                .empty(page.empty())
                .totalElements(page.totalElements())
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .first(page.first())
                .last(page.last())
                .empty(page.empty())
                .totalPages(page.totalPages())
                .build();
    }

    public static BlogDescriptionDto convert(BlogDescriptionDto content) {
        return BlogDescriptionDto.builder()
                .id(content.id())
                .status(content.status())
                .link(content.link())
                .created(content.created())
                .title(content.title())
                .description(content.description())
                .build();
    }

}
