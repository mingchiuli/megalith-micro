package org.chiu.micro.common.dto;

import java.time.LocalDateTime;


public record RoleEntityRpcDto(

        Long id,

        String name,

        String code,

        String remark,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status) {
}

