package org.chiu.micro.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:47 am
 */
@Data
@Builder
public class LoginSuccessVo implements Serializable {
    private String accessToken;

    private String refreshToken;
}
