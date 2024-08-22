package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name ="m_user",
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEntity that = (UserEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(username, that.username)) return false;
        if (!Objects.equals(nickname, that.nickname)) return false;
        if (!Objects.equals(avatar, that.avatar)) return false;
        if (!Objects.equals(email, that.email)) return false;
        if (!Objects.equals(phone, that.phone)) return false;
        if (!Objects.equals(password, that.password)) return false;
        if (!Objects.equals(status, that.status)) return false;
        if (!Objects.equals(created, that.created)) return false;
        if (!Objects.equals(lastLogin, that.lastLogin)) return false;
        return Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public UserEntity(Long id, String nickname, String avatar) {
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
    }
}
