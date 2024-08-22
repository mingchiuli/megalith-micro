package org.chiu.micro.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-15 2:14 pm
 */

@Data
@Builder
public class UserInfoVo implements Serializable {

    private String nickname;

    private String avatar;
}
