package wiki.chiu.micro.user.vo;

import java.time.LocalDateTime;

public record UserEntityRpcVo(

        Long id,

        String username,

        String nickname,

        String password,

        String avatar,

        String email,

        String phone,

        Integer status,

        LocalDateTime created,

        LocalDateTime updated,

        LocalDateTime lastLogin) {

    public static UserEntityRpcVoBuilder builder() {
        return new UserEntityRpcVoBuilder();
    }

    public static class UserEntityRpcVoBuilder {
        private Long id;
        private String username;
        private String nickname;
        private String password;
        private String avatar;
        private String email;
        private String phone;
        private Integer status;
        private LocalDateTime created;
        private LocalDateTime updated;
        private LocalDateTime lastLogin;

        public UserEntityRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserEntityRpcVoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserEntityRpcVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserEntityRpcVoBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserEntityRpcVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserEntityRpcVoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserEntityRpcVoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserEntityRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public UserEntityRpcVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public UserEntityRpcVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public UserEntityRpcVoBuilder lastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public UserEntityRpcVo build() {
            return new UserEntityRpcVo(id, username, nickname, password, avatar, email, phone, status, created, updated, lastLogin);
        }
    }
}
