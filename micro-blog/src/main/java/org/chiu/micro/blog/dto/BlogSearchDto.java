package org.chiu.micro.blog.dto;

import java.util.List;

import lombok.Data;

@Data
public class BlogSearchDto {
    
    private Long total;

    private Integer currentPage;

    private Integer size;

    private List<Long> ids;
}
