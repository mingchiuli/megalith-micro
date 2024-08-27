package org.chiu.micro.gateway.dto;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthorityDto {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private String prototype;

    private String methodType;

    private String routePattern;

    private String requestHost;
    
    private String requestPort;

    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer status;
}
