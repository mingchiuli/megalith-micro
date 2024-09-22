package org.chiu.micro.search.dto;

import java.util.List;

public record AuthDto(

        Long userId,

        List<String> roles,

        List<String> authorities) {
}
