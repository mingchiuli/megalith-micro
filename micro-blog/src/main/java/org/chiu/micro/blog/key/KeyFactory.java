package org.chiu.micro.blog.key;

import java.util.Objects;

import static org.chiu.micro.blog.lang.Const.TEMP_EDIT_BLOG;

public class KeyFactory {

    private KeyFactory() {}

    public static String createBlogEditRedisKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                TEMP_EDIT_BLOG.getInfo() + "init:" + userId :
                TEMP_EDIT_BLOG.getInfo() + blogId;
    }
}
