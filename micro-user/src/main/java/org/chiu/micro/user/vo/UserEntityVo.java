package org.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record UserEntityVo(

        Long id,

        String username,

        String nickname,

        String avatar,

        String email,

        String phone,

        Integer status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updated,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLogin,

        List<String> roles) {

    public static UserEntityVoBuilder builder() {
        return new UserEntityVoBuilder();
    }

    public static class UserEntityVoBuilder {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String email;
        private String phone;
        private Integer status;
        private LocalDateTime created;
        private LocalDateTime updated;
        private LocalDateTime lastLogin;
        private List<String> roles;

        public UserEntityVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserEntityVoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserEntityVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserEntityVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserEntityVoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserEntityVoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserEntityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public UserEntityVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public UserEntityVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public UserEntityVoBuilder lastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public UserEntityVoBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public UserEntityVo build() {
            return new UserEntityVo(id, username, nickname, avatar, email, phone, status, created, updated, lastLogin, roles);
        }
    }
}
