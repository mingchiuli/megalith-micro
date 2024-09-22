package org.chiu.micro.exhibit.dto;

import java.util.List;

public record AuthDto(

        Long userId,

        List<String> roles,

        List<String> authorities) {
}
