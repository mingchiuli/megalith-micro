package org.chiu.micro.user.vo;

import java.time.LocalDateTime;

public class UserEntityRpcVo {

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

    UserEntityRpcVo(Long id, String username, String nickname, String password, String avatar, String email, String phone, Integer status, LocalDateTime created, LocalDateTime updated, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.avatar = avatar;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.created = created;
        this.updated = updated;
        this.lastLogin = lastLogin;
    }

    public static UserEntityRpcVoBuilder builder() {
        return new UserEntityRpcVoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public Integer getStatus() {
        return this.status;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public LocalDateTime getLastLogin() {
        return this.lastLogin;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserEntityRpcVo)) return false;
        final UserEntityRpcVo other = (UserEntityRpcVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        final Object this$nickname = this.getNickname();
        final Object other$nickname = other.getNickname();
        if (this$nickname == null ? other$nickname != null : !this$nickname.equals(other$nickname)) return false;
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
        final Object this$avatar = this.getAvatar();
        final Object other$avatar = other.getAvatar();
        if (this$avatar == null ? other$avatar != null : !this$avatar.equals(other$avatar)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$phone = this.getPhone();
        final Object other$phone = other.getPhone();
        if (this$phone == null ? other$phone != null : !this$phone.equals(other$phone)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        final Object this$lastLogin = this.getLastLogin();
        final Object other$lastLogin = other.getLastLogin();
        if (this$lastLogin == null ? other$lastLogin != null : !this$lastLogin.equals(other$lastLogin)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserEntityRpcVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        final Object $nickname = this.getNickname();
        result = result * PRIME + ($nickname == null ? 43 : $nickname.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        final Object $avatar = this.getAvatar();
        result = result * PRIME + ($avatar == null ? 43 : $avatar.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $phone = this.getPhone();
        result = result * PRIME + ($phone == null ? 43 : $phone.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        final Object $lastLogin = this.getLastLogin();
        result = result * PRIME + ($lastLogin == null ? 43 : $lastLogin.hashCode());
        return result;
    }

    public String toString() {
        return "UserEntityRpcVo(id=" + this.getId() + ", username=" + this.getUsername() + ", nickname=" + this.getNickname() + ", password=" + this.getPassword() + ", avatar=" + this.getAvatar() + ", email=" + this.getEmail() + ", phone=" + this.getPhone() + ", status=" + this.getStatus() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ", lastLogin=" + this.getLastLogin() + ")";
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

        UserEntityRpcVoBuilder() {
        }

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
            return new UserEntityRpcVo(this.id, this.username, this.nickname, this.password, this.avatar, this.email, this.phone, this.status, this.created, this.updated, this.lastLogin);
        }

        public String toString() {
            return "UserEntityRpcVo.UserEntityRpcVoBuilder(id=" + this.id + ", username=" + this.username + ", nickname=" + this.nickname + ", password=" + this.password + ", avatar=" + this.avatar + ", email=" + this.email + ", phone=" + this.phone + ", status=" + this.status + ", created=" + this.created + ", updated=" + this.updated + ", lastLogin=" + this.lastLogin + ")";
        }
    }
}
