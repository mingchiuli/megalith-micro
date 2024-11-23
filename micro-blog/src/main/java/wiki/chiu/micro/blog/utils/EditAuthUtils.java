package wiki.chiu.micro.blog.utils;

import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;

import java.util.Objects;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EDIT_NO_AUTH;


public class EditAuthUtils {

    private EditAuthUtils() {
    }

    public static void checkEditAuth(BlogEntity blogEntity, Long userId) {

        if (!Objects.equals(StatusEnum.NORMAL.getCode(), blogEntity.getStatus()) && !Objects.equals(blogEntity.getUserId(), userId)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }
}
