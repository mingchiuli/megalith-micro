package org.chiu.micro.gateway.vo;

import lombok.Data;

@Data
public class BlogExhibitVo {

    private String description;

    private String nickname;

    private String avatar;

    private String title;

    private String content;

    private String created;

    private Long readCount;
}