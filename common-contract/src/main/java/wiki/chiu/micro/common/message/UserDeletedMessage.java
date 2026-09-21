package wiki.chiu.micro.common.message;

import java.util.List;

public record UserDeletedMessage(String eventId, List<Long> userIds) {

    public UserDeletedMessage {
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
    }
}
