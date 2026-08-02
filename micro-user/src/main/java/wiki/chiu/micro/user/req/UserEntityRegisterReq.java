package wiki.chiu.micro.user.req;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import wiki.chiu.micro.user.valid.PasswordMatches;
import wiki.chiu.micro.user.valid.CrossFieldValidation;

import static wiki.chiu.micro.common.lang.Const.EMAIL_REGEX;
import static wiki.chiu.micro.common.lang.Const.USERNAME_REGEX;

@PasswordMatches(groups = CrossFieldValidation.class)
@GroupSequence({UserEntityRegisterReq.class, CrossFieldValidation.class})
public record UserEntityRegisterReq(

        @NotBlank(message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        @Pattern(regexp = USERNAME_REGEX, message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        String username,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        String nickname,

        String avatar,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        String password,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        String confirmPassword,

        @NotNull(message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        @Pattern(regexp = EMAIL_REGEX, message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        String email,

        String phone,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.RegisterSave.message}")
        String token) {

        public UserEntityRegisterReq(UserEntityRegisterReq req, String phone) {
                this(req.username, req.nickname, req.avatar, req.password, req.confirmPassword, req.email, phone, req.token);
        }
}
