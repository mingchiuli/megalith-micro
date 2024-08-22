package org.chiu.micro.gateway.vo;

import lombok.*;


@Data
@Builder
public class AuthorityVo {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private String created;

    private String updated;

    private Integer status;
}
