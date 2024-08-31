package org.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthorityVo {

    private Long id;

    private String name;

    private String code;

    private String remark;

    private String prototype;

    private String methodType;

    private String routePattern;

    private String serviceHost;

    private Integer servicePort;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    private Integer status;
}
