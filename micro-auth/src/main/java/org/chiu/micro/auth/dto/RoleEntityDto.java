package org.chiu.micro.auth.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RoleEntityDto {
    
    private Long id;

    private String name;

    private String code;

    private String remark;

    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer status;
}
