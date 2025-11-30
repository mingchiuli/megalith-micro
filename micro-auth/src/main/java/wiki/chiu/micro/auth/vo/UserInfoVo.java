package wiki.chiu.micro.auth.vo;


/**
 * @author mingchiuli
 * @create 2023-04-15 2:14 pm
 */

public record UserInfoVo(

        Long id,

        String nickname,

        String avatar) {

    public static UserInfoVoBuilder builder() {
        return new UserInfoVoBuilder();
    }

    public static class UserInfoVoBuilder {
        private Long id;
        private String nickname;
        private String avatar;

        public UserInfoVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserInfoVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserInfoVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserInfoVo build() {
            return new UserInfoVo(id, nickname, avatar);
        }

    }
}
