package org.chiu.micro.user.constant;



import java.io.Serializable;

import org.chiu.micro.user.lang.UserOperateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserIndexMessage implements Serializable {

    private Long userId;

    private UserOperateEnum userOperateEnum;

}
