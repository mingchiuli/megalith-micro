package org.chiu.micro.blog.dto;

import java.util.List;

public record BlogSearchDto(

        Long total,

        Integer currentPage,

        Integer size,

        List<Long> ids) {
}
