package org.chiu.micro.search.dto;

import java.util.List;

import lombok.Data;

@Data
public class AuthDto {

    private Long userId;

    private List<String> roles;

    private List<String> authorities;
    
}
