package org.chiu.micro.auth.dto;

import java.time.LocalDateTime;


public record UserEntityDto(
        Long id,

        String username,

        String password,

        String nickname,

        String avatar,

        String email,

        String phone,

        Integer status,

        LocalDateTime created,

        LocalDateTime updated,

        LocalDateTime lastLogin) {
}
