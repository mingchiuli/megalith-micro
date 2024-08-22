package org.chiu.micro.exhibit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogSensitiveContentDto implements Serializable {

    private Long blogId;

    private List<SensitiveContent> sensitiveContent;

}
