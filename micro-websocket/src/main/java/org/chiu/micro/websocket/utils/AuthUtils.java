package org.chiu.micro.websocket.utils;

import java.util.Objects;

import org.chiu.micro.websocket.dto.BlogEntityDto;
import org.chiu.micro.websocket.lang.StatusEnum;
import org.springframework.util.Assert;
import static org.chiu.micro.websocket.lang.ExceptionMessage.*;


public class AuthUtils {

    private AuthUtils() {}
    
    public static void checkEditAuth(BlogEntityDto blogEntityDto, Long userId) {

        Assert.isTrue(Objects.equals(StatusEnum.NORMAL.getCode(), blogEntityDto.getStatus()) || Objects.equals(blogEntityDto.getUserId(), userId), EDIT_NO_AUTH.getMsg());

    }
}
