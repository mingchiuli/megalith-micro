package org.chiu.micro.gateway.req;

import lombok.Data;


@Data
public class AuthorityEntityReq {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private Integer status;

    private String prototype;

    private String methodType;

    private String routePattern;

    private String requestHost;
    
    private String requestPort;
}
