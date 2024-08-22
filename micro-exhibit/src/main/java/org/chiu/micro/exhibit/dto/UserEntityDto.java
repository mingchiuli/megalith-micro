package org.chiu.micro.exhibit.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserEntityDto {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    private LocalDateTime created;

    private LocalDateTime updated;

    private LocalDateTime lastLogin;
}
