package org.chiu.micro.search.vo;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogSearchVo {
    
    private Long total;

    private Integer currentPage;

    private Integer size;

    private List<Long> ids;
}
