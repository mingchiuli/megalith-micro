package org.chiu.micro.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.chiu.micro.user.valid.Phone;
import org.chiu.micro.user.valid.Username;

public record UserEntityRegisterReq(

        Long id,

        @Username
        String username,

        @NotBlank
        String nickname,

        String avatar,

        @NotBlank
        String password,

        @NotBlank
        String confirmPassword,

        @Email
        String email,

        @Phone
        String phone) {

        public UserEntityRegisterReq(UserEntityRegisterReq req, String phone) {
                this(req.id, req.username, req.nickname, req.avatar, req.password, req.confirmPassword, req.email, phone);
        }
}
