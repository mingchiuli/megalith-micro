package org.chiu.micro.exhibit.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author mingchiuli
 * @create 2023-03-30 3:15 am
 */
@Builder
@Data
public class BlogHotReadVo {

    private Long id;

    private String title;

    private Long readCount;
}
