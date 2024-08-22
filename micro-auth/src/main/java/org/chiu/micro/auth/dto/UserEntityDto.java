package org.chiu.micro.auth.dto;

import java.time.LocalDateTime;
import lombok.Data;


@Data
public class UserEntityDto {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    private LocalDateTime created;

    private LocalDateTime updated;

    private LocalDateTime lastLogin;
}
