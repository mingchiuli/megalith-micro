package wiki.chiu.micro.common.lang;

import java.util.List;

public record AuthCacheEvictMessage(
    String eventId,
    List<Long> userIds,
    List<Long> roleIds,
    List<String> roleCodes,
    boolean evictMenus,
    boolean evictRoutes) {

  public AuthCacheEvictMessage {
    userIds = userIds == null ? List.of() : List.copyOf(userIds);
    roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
    roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
  }
}
