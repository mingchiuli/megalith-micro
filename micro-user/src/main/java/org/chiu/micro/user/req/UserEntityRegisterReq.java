package org.chiu.micro.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.chiu.micro.user.valid.Phone;
import org.chiu.micro.user.valid.Username;

public class UserEntityRegisterReq {

    private Long id;

    @Username
    private String username;

    @NotBlank
    private String nickname;

    private String avatar;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    @Email
    private String email;

    @Phone
    private String phone;

    public UserEntityRegisterReq() {
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

    public String getAvatar() {
        return this.avatar;
    }

    public @NotBlank String getPassword() {
        return this.password;
    }

    public @NotBlank String getConfirmPassword() {
        return this.confirmPassword;
    }

    public @Email String getEmail() {
        return this.email;
    }

    public @Phone String getPhone() {
        return this.phone;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }

    public void setConfirmPassword(@NotBlank String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }

    public void setPhone(@Phone String phone) {
        this.phone = phone;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserEntityRegisterReq)) return false;
        final UserEntityRegisterReq other = (UserEntityRegisterReq) o;
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
        final Object this$confirmPassword = this.getConfirmPassword();
        final Object other$confirmPassword = other.getConfirmPassword();
        if (this$confirmPassword == null ? other$confirmPassword != null : !this$confirmPassword.equals(other$confirmPassword))
            return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$phone = this.getPhone();
        final Object other$phone = other.getPhone();
        if (this$phone == null ? other$phone != null : !this$phone.equals(other$phone)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserEntityRegisterReq;
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
        final Object $confirmPassword = this.getConfirmPassword();
        result = result * PRIME + ($confirmPassword == null ? 43 : $confirmPassword.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $phone = this.getPhone();
        result = result * PRIME + ($phone == null ? 43 : $phone.hashCode());
        return result;
    }

    public String toString() {
        return "UserEntityRegisterReq(id=" + this.getId() + ", username=" + this.getUsername() + ", nickname=" + this.getNickname() + ", avatar=" + this.getAvatar() + ", password=" + this.getPassword() + ", confirmPassword=" + this.getConfirmPassword() + ", email=" + this.getEmail() + ", phone=" + this.getPhone() + ")";
    }
}
