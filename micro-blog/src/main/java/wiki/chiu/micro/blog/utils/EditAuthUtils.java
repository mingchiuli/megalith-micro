package wiki.chiu.micro.blog.utils;

import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import java.util.List;
import java.util.Objects;

import static wiki.chiu.micro.common.lang.ExceptionMessage.EDIT_NO_AUTH;


public class EditAuthUtils {

    private EditAuthUtils() {
    }

    public static void checkEditAuth(BlogEntity blogEntity, Long userId) {

        if (!Objects.equals(BlogStatusEnum.NORMAL.getCode(), blogEntity.getStatus()) && !Objects.equals(BlogStatusEnum.DRAFT.getCode(), blogEntity.getStatus()) && !Objects.equals(blogEntity.getUserId(), userId)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }

    public static void checkSaveAuth(BlogEntity blogEntity, Long userId, List<String> roles, String highestRole) {

        if (!roles.contains(highestRole) && !Objects.equals(blogEntity.getUserId(), userId)) {
            throw new MissException(EDIT_NO_AUTH.getMsg());
        }
    }
}
