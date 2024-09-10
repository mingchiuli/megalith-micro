package org.chiu.micro.blog.utils;

import java.util.Objects;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.lang.StatusEnum;
import org.springframework.util.Assert;
import static org.chiu.micro.blog.lang.ExceptionMessage.*;


public class AuthUtils {

    private AuthUtils() {}
    
    public static void checkEditAuth(BlogEntity blogEntity, Long userId) {

        Assert.isTrue(Objects.equals(StatusEnum.NORMAL.getCode(), blogEntity.getStatus()) || Objects.equals(blogEntity.getUserId(), userId), EDIT_NO_AUTH.getMsg());

    }
}
