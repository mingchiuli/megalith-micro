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
        LocalDateTime lastLogin
) {

    public static UserEntityRpcVoBuilder builder() {
        return new UserEntityRpcVoBuilder(null, null, null, null, null, null, null, null, null, null, null);
    }

    public record UserEntityRpcVoBuilder(
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
            LocalDateTime lastLogin
    ) {

        public UserEntityRpcVoBuilder id(Long id) {
            return new UserEntityRpcVoBuilder(id, this.username, this.password, this.nickname, this.avatar, this.email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder username(String username) {
            return new UserEntityRpcVoBuilder(this.id, username, this.password, this.nickname, this.avatar, this.email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder password(String password) {
            return new UserEntityRpcVoBuilder(this.id, this.username, password, this.nickname, this.avatar, this.email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder nickname(String nickname) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, nickname, this.avatar, this.email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder avatar(String avatar) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, avatar, this.email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder email(String email) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, this.avatar, email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder phone(String phone) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, this.avatar, this.email, phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder status(Integer status) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, this.avatar, this.email, this.phone, status, this.created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder created(LocalDateTime created) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, this.avatar, this.email, this.phone, this.status, created, this.updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder updated(LocalDateTime updated) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, this.avatar, this.email, this.phone, this.status, this.created, updated, this.lastLogin);
        }

        public UserEntityRpcVoBuilder lastLogin(LocalDateTime lastLogin) {
            return new UserEntityRpcVoBuilder(this.id, this.username, this.password, this.nickname, this.avatar, this.email, this.phone, this.status, this.created, this.updated, lastLogin);
        }

        public UserEntityRpcVo build() {
            return new UserEntityRpcVo(id, username, password, nickname, avatar, email, phone, status, created, updated, lastLogin);
        }
    }
}