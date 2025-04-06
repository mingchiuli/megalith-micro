package wiki.chiu.micro.common.vo;

import java.time.LocalDateTime;


public record UserEntityRpcVo(
        Long id,

        String username,

        String password,

        String nickname,

        String avatar,

        String email,

        String phone,

        Integer status,

        LocalDateTime created,

        LocalDateTime updated,

        LocalDateTime lastLogin) {


    public static UserEntityRpcVo.UserEntityRpcVoBuilder builder() {
        return new UserEntityRpcVo.UserEntityRpcVoBuilder();
    }

    public static class UserEntityRpcVoBuilder {
        private Long id;

        private String username;

        private String password;

        private String nickname;

        private String avatar;

        private String email;

        private String phone;

        private Integer status;

        private LocalDateTime created;

        private LocalDateTime updated;

        private LocalDateTime lastLogin;


        public UserEntityRpcVo.UserEntityRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public UserEntityRpcVo.UserEntityRpcVoBuilder lastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public UserEntityRpcVo build() {
            return new UserEntityRpcVo(id, username, password, nickname, avatar, email, phone, status, created, updated, lastLogin);
        }

    }

}
