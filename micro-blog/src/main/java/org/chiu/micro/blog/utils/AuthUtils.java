package org.chiu.micro.blog.utils;

import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.common.lang.StatusEnum;

import java.util.Objects;

import static org.chiu.micro.common.lang.ExceptionMessage.EDIT_NO_AUTH;


public class AuthUtils {

    private AuthUtils() {
    }

    public static void checkEditAuth(BlogEntity blogEntity, Long userId) {

        if (!Objects.equals(StatusEnum.NORMAL.getCode(), blogEntity.getStatus()) && !Objects.equals(blogEntity.getUserId(), userId)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }
}
