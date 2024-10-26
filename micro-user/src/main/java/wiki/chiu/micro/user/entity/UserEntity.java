package wiki.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:18 am
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_user",
        indexes = {@Index(columnList = "created")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"username"}), @UniqueConstraint(columnNames = {"email"}), @UniqueConstraint(columnNames = {"phone"})})
public class UserEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public UserEntity(Long id, String nickname, String avatar) {
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public UserEntity(Long id, String username, String nickname, String avatar, String email, String phone, String password, Integer status, LocalDateTime created, LocalDateTime updated, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = status;
        this.created = created;
        this.updated = updated;
        this.lastLogin = lastLogin;
    }

    public UserEntity() {
    }

    public static UserEntityBuilder builder() {
        return new UserEntityBuilder();
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

    public String getAvatar() {
        return this.avatar;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getPassword() {
        return this.password;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(nickname, that.nickname) && Objects.equals(avatar, that.avatar) && Objects.equals(email, that.email) && Objects.equals(phone, that.phone) && Objects.equals(password, that.password) && Objects.equals(status, that.status) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated) && Objects.equals(lastLogin, that.lastLogin);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(username);
        result = 31 * result + Objects.hashCode(nickname);
        result = 31 * result + Objects.hashCode(avatar);
        result = 31 * result + Objects.hashCode(email);
        result = 31 * result + Objects.hashCode(phone);
        result = 31 * result + Objects.hashCode(password);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        result = 31 * result + Objects.hashCode(lastLogin);
        return result;
    }

    public static class UserEntityBuilder {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String email;
        private String phone;
        private String password;
        private Integer status;
        private LocalDateTime created;
        private LocalDateTime updated;
        private LocalDateTime lastLogin;

        UserEntityBuilder() {
        }

        public UserEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserEntityBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserEntityBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserEntityBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserEntityBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserEntityBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserEntityBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public UserEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public UserEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public UserEntityBuilder lastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public UserEntity build() {
            return new UserEntity(id, username, nickname, avatar, email, phone, password, status, created, updated, lastLogin);
        }
    }
}
