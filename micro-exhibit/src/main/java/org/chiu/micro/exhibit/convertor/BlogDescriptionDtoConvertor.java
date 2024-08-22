package org.chiu.micro.exhibit.convertor;

import java.util.ArrayList;
import java.util.List;

import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.page.PageAdapter;

/**
 * @Author limingjiu
 * @Date 2024/5/10 11:16
 **/
public class BlogDescriptionDtoConvertor {

    private BlogDescriptionDtoConvertor() {}

    public static PageAdapter<BlogDescriptionDto> convert(PageAdapter<BlogEntityDto> page) {
        List<BlogEntityDto> content = page.getContent();
        List<BlogDescriptionDto> dtos = new ArrayList<>();
        content.forEach(item -> dtos.add(BlogDescriptionDto.builder()
                .id(item.getId())
                .description(item.getDescription())
                .title(item.getTitle())
                .status(item.getStatus())
                .created(item.getCreated())
                .link(item.getLink())
                .build()));

        return PageAdapter.<BlogDescriptionDto>builder()
                .content(dtos)
                .empty(page.isEmpty())
                .totalElements(page.getTotalElements())
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .totalPages(page.getTotalPages())
                .build();
    }

    public static BlogDescriptionDto convert(BlogDescriptionDto content) {
        return BlogDescriptionDto.builder()
                .id(content.getId())
                .status(content.getStatus())
                .link(content.getLink())
                .created(content.getCreated())
                .title(content.getTitle())
                .description(content.getDescription())
                .build();
    }

}
