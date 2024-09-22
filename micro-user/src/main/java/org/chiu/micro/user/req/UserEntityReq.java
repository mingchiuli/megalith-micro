package org.chiu.micro.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.chiu.micro.user.valid.ListValue;
import org.chiu.micro.user.valid.Phone;
import org.chiu.micro.user.valid.Username;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record UserEntityReq(

        Long id,

        @Username
        String username,

        @NotBlank
        String nickname,

        @URL
        String avatar,

        String password,

        @Email
        String email,

        @Phone
        String phone,

        @ListValue(values = {0, 1})
        Integer status,

        @NotEmpty
        List<String> roles) {
}
