package org.chiu.micro.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.chiu.micro.user.valid.ListValue;
import org.chiu.micro.user.valid.Phone;
import org.chiu.micro.user.valid.Username;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class UserEntityReq {

    private Long id;

    @Username
    private String username;

    @NotBlank
    private String nickname;

    @URL
    private String avatar;

    private String password;

    @Email
    private String email;

    @Phone
    private String phone;

    @ListValue(values = {0, 1})
    private Integer status;

    @NotEmpty
    private List<String> roles;

    public UserEntityReq() {
    }

    public Long getId() {
        return this.id;
    }

    public @Username String getUsername() {
        return this.username;
    }

    public @NotBlank String getNickname() {
        return this.nickname;
    }

    public @URL String getAvatar() {
        return this.avatar;
    }

    public String getPassword() {
        return this.password;
    }

    public @Email String getEmail() {
        return this.email;
    }

    public @Phone String getPhone() {
        return this.phone;
    }

    public @ListValue(values = {0, 1}) Integer getStatus() {
        return this.status;
    }

    public @NotEmpty List<String> getRoles() {
        return this.roles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(@Username String username) {
        this.username = username;
    }

    public void setNickname(@NotBlank String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(@URL String avatar) {
        this.avatar = avatar;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }

    public void setPhone(@Phone String phone) {
        this.phone = phone;
    }

    public void setStatus(@ListValue(values = {0, 1}) Integer status) {
        this.status = status;
    }

    public void setRoles(@NotEmpty List<String> roles) {
        this.roles = roles;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserEntityReq)) return false;
        final UserEntityReq other = (UserEntityReq) o;
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
        final Object this$avatar = this.getAvatar();
        final Object other$avatar = other.getAvatar();
        if (this$avatar == null ? other$avatar != null : !this$avatar.equals(other$avatar)) return false;
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$phone = this.getPhone();
        final Object other$phone = other.getPhone();
        if (this$phone == null ? other$phone != null : !this$phone.equals(other$phone)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$roles = this.getRoles();
        final Object other$roles = other.getRoles();
        if (this$roles == null ? other$roles != null : !this$roles.equals(other$roles)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserEntityReq;
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
        final Object $avatar = this.getAvatar();
        result = result * PRIME + ($avatar == null ? 43 : $avatar.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $phone = this.getPhone();
        result = result * PRIME + ($phone == null ? 43 : $phone.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $roles = this.getRoles();
        result = result * PRIME + ($roles == null ? 43 : $roles.hashCode());
        return result;
    }

    public String toString() {
        return "UserEntityReq(id=" + this.getId() + ", username=" + this.getUsername() + ", nickname=" + this.getNickname() + ", avatar=" + this.getAvatar() + ", password=" + this.getPassword() + ", email=" + this.getEmail() + ", phone=" + this.getPhone() + ", status=" + this.getStatus() + ", roles=" + this.getRoles() + ")";
    }
}
