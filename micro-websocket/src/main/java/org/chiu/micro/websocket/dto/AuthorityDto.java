package org.chiu.micro.websocket.dto;


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

    private String serviceName;
    
    private Integer status;
}
