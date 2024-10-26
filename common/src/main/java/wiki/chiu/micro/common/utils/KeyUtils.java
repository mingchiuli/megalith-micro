package wiki.chiu.micro.common.utils;

import java.util.Objects;

import static wiki.chiu.micro.common.lang.Const.TEMP_EDIT_BLOG;

public class KeyUtils {

    private KeyUtils() {}

    public static String createBlogEditRedisKey(Long userId, Long blogId) {
        return Objects.isNull(blogId) ?
                TEMP_EDIT_BLOG + "init:" + userId :
                TEMP_EDIT_BLOG + blogId;
    }
}
