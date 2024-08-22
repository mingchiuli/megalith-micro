package org.chiu.micro.blog.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BlogSensitiveContentDto {

    private Long blogId;

    private List<String> sensitiveContent;

}
