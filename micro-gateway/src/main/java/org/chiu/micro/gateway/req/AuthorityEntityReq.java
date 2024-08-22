package org.chiu.micro.gateway.req;

import lombok.Data;


@Data
public class AuthorityEntityReq {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private Integer status;
}
