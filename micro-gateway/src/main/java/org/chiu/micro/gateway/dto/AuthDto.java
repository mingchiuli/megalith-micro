package org.chiu.micro.gateway.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthDto {

    private Long userId;

    private List<String> roles;

    private List<String> authorities;
    
}
