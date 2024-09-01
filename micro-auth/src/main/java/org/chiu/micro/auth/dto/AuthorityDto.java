package org.chiu.micro.auth.dto;


import lombok.*;

@Data
public class AuthorityDto {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private String prototype;

    private String methodType;

    private String routePattern;

    private String serviceHost;

    private Integer servicePort;
    
    private Integer status;
}
