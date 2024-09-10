package org.chiu.micro.websocket.key;

import java.util.Objects;

import static org.chiu.micro.websocket.lang.Const.TEMP_EDIT_BLOG;

public class KeyFactory {

    private KeyFactory() {}

    public static String createBlogEditRedisKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                TEMP_EDIT_BLOG.getInfo() + "init:" + userId :
                TEMP_EDIT_BLOG.getInfo() + blogId;
    }

    public static String createSubscriptionKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                "/init/" + userId :
                "/" + blogId;
    }
}
