package org.chiu.micro.common.dto;

import java.util.List;

public record BlogSearchRpcDto(

        Long total,

        Integer currentPage,

        Integer size,

        List<Long> ids) {
}
