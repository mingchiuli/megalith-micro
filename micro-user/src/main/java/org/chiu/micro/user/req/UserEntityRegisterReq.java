package org.chiu.micro.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.chiu.micro.user.valid.Phone;
import org.chiu.micro.user.valid.Username;

@Data
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
}
