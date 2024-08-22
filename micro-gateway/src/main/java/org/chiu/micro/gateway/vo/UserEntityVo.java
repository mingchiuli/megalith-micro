package org.chiu.micro.gateway.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserEntityVo {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private Integer status;

    private String created;

    private String updated;

    private String lastLogin;

    private List<String> roles;
}
