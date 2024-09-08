package org.chiu.micro.auth.vo;


import lombok.*;

@Data
@Builder
public class AuthorityRouteVo {

    private Boolean auth;

    private String serviceHost;

    private Integer servicePort;
}
