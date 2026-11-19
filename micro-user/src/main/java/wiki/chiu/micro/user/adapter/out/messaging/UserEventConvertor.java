package wiki.chiu.micro.user.adapter.out.messaging;

import java.util.List;

import wiki.chiu.micro.common.message.AuthCacheEvictMessage;
import wiki.chiu.micro.common.message.UserDeletedMessage;

public final class UserEventConvertor {

    private UserEventConvertor() {
    }

    public static AuthCacheEvictMessage toAuthCacheEvictMessage(
        String eventId,
        List<Long> userIds,
        List<Long> roleIds,
        List<String> roleCodes,
        boolean evictMenus,
        boolean evictRoutes) {
        return new AuthCacheEvictMessage(
            eventId, userIds, roleIds, roleCodes, evictMenus, evictRoutes);
    }

    public static UserDeletedMessage toUserDeletedMessage(String eventId, List<Long> userIds) {
        return new UserDeletedMessage(eventId, userIds);
    }
}
