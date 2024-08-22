package org.chiu.micro.user.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEntityRpcVo {

    private Long id;

    private String username;

    private String nickname;

    private String password;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    private LocalDateTime created;

    private LocalDateTime updated;

    private LocalDateTime lastLogin;
}
