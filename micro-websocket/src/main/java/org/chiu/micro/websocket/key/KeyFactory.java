package org.chiu.micro.websocket.key;

import java.util.Objects;

import static org.chiu.micro.websocket.lang.Const.TEMP_EDIT_BLOG;

public class KeyFactory {

    private KeyFactory() {}

    public static String createBlogEditRedisKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                TEMP_EDIT_BLOG.getInfo() + userId :
                TEMP_EDIT_BLOG.getInfo() + userId + ":" + blogId;
    }

    public static String createSubscriptionKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                String.valueOf(userId) :
                userId + "/" + blogId;
    }
}
