package org.chiu.micro.common.utils;

import java.util.Objects;

import static org.chiu.micro.common.lang.Const.TEMP_EDIT_BLOG;

public class KeyUtils {

    private KeyUtils() {}

    public static String createBlogEditRedisKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                TEMP_EDIT_BLOG.getInfo() + "init:" + userId :
                TEMP_EDIT_BLOG.getInfo() + blogId;
    }
}
