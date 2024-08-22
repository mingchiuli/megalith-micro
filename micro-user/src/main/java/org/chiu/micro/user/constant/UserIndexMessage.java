package org.chiu.micro.user.constant;



import org.chiu.micro.user.lang.UserOperateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserIndexMessage {

    private Long userId;

    private UserOperateEnum userOperateEnum;

}
