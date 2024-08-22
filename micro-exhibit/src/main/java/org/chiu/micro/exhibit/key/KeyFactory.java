package org.chiu.micro.exhibit.key;

import java.util.Objects;

import static org.chiu.micro.exhibit.lang.Const.TEMP_EDIT_BLOG;

public class KeyFactory {

    private KeyFactory() {}

    public static String createBlogEditRedisKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                TEMP_EDIT_BLOG.getInfo() + userId :
                TEMP_EDIT_BLOG.getInfo() + userId + ":" + blogId;
    }
}
