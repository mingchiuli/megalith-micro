package org.chiu.micro.gateway.dto;


import lombok.*;

@Data
public class AuthorityRouteDto {

    private Boolean auth;

    private String serviceHost;

    private Integer servicePort;
}
