package wiki.chiu.micro.common.dto;

import java.time.LocalDateTime;


public record UserEntityRpcDto(
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
