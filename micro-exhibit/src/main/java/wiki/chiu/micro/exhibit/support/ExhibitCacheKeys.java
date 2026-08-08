package wiki.chiu.micro.exhibit.support;

import static wiki.chiu.micro.common.lang.Const.TEMP_EDIT_BLOG;

import java.util.Objects;

public final class ExhibitCacheKeys {

  private ExhibitCacheKeys() {}

  public static String createBlogEditRedisKey(Long userId, Long blogId) {
    return Objects.isNull(blogId) ? TEMP_EDIT_BLOG + "init:" + userId : TEMP_EDIT_BLOG + blogId;
  }
}
