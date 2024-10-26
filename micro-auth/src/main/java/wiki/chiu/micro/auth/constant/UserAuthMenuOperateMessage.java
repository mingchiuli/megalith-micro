package wiki.chiu.micro.auth.constant;

import java.io.Serializable;
import java.util.List;

/**
 * UserAuthMenuOperateMessage
 */
public record UserAuthMenuOperateMessage(
        List<String> roles,

        Integer type) implements Serializable {

    public static UserAuthMenuOperateMessageBuilder builder() {
        return new UserAuthMenuOperateMessageBuilder();
    }

    public static class UserAuthMenuOperateMessageBuilder {
        private List<String> roles;
        private Integer type;

        public UserAuthMenuOperateMessageBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public UserAuthMenuOperateMessageBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public UserAuthMenuOperateMessage build() {
            return new UserAuthMenuOperateMessage(roles, type);
        }

    }
}