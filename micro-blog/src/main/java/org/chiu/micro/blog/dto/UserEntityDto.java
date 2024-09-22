package org.chiu.micro.blog.dto;

import java.time.LocalDateTime;


public record UserEntityDto(

        Long id,

        String username,

        String nickname,

        String avatar,

        String email,

        String phone,

        Integer status,

        LocalDateTime created,

        LocalDateTime updated,

        LocalDateTime lastLogin) {
}
