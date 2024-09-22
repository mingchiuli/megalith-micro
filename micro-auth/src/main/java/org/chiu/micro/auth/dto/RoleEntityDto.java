package org.chiu.micro.auth.dto;

import java.time.LocalDateTime;


public record RoleEntityDto(

        Long id,

        String name,

        String code,

        String remark,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status) {
}

