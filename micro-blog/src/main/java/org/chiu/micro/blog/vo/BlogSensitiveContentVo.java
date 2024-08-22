package org.chiu.micro.blog.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BlogSensitiveContentVo {

    private Long blogId;

    private List<SensitiveContentVo> sensitiveContent;

}
