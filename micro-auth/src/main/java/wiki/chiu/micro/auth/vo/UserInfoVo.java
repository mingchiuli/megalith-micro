package wiki.chiu.micro.auth.vo;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-15 2:14 pm
 */

public record UserInfoVo(

        Long id,

        String nickname,

        String avatar) implements Serializable {

    public static UserInfoVoBuilder builder() {
        return new UserInfoVoBuilder(null, null, null);
    }

    public record UserInfoVoBuilder(
             Long id,
             String nickname,
             String avatar
    ) {

        public UserInfoVoBuilder nickname(String nickname) {
            return new UserInfoVoBuilder(id, nickname, avatar);
        }

        public UserInfoVoBuilder avatar(String avatar) {
            return new UserInfoVoBuilder(id, nickname, avatar);
        }

        public UserInfoVoBuilder id(Long id) {
            return new UserInfoVoBuilder(id, nickname, avatar);
        }

        public UserInfoVo build() {
            return new UserInfoVo(id, nickname, avatar);
        }

    }
}
