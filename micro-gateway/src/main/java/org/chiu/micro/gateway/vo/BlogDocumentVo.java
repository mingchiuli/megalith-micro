package org.chiu.micro.gateway.vo;


import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author mingchiuli
 * @create 2021-12-12 6:55 AM
 */
@Data
public class BlogDocumentVo {

    private Long id;

    private Long userId;

    private Integer status;

    private String title;

    private String description;

    private String content;

    private String link;

    private String created;

    private Float score;

    private Map<String, List<String>> highlight;

}
