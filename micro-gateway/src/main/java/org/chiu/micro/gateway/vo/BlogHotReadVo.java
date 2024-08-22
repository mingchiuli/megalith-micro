package org.chiu.micro.gateway.vo;

import lombok.Data;

/**
 * @author mingchiuli
 * @create 2023-03-30 3:15 am
 */
@Data
public class BlogHotReadVo {

    private Long id;

    private String title;

    private Long readCount;
}
