package org.chiu.micro.search.dto;

import java.time.LocalDateTime;


public record BlogEntityDto(

        Long id,

        Long userId,

        String title,

        String description,

        String content,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status,

        String link,

        Long readCount) {
}
