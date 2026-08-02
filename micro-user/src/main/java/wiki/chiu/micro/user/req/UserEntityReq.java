package wiki.chiu.micro.user.req;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;
import wiki.chiu.micro.user.valid.PasswordRequiredForCreate;
import wiki.chiu.micro.user.valid.CrossFieldValidation;

import static wiki.chiu.micro.common.lang.Const.EMAIL_REGEX;
import static wiki.chiu.micro.common.lang.Const.PHONE_REGEX;
import static wiki.chiu.micro.common.lang.Const.URL_REGEX;
import static wiki.chiu.micro.common.lang.Const.USERNAME_REGEX;

@PasswordRequiredForCreate(groups = CrossFieldValidation.class)
@GroupSequence({UserEntityReq.class, CrossFieldValidation.class})
public record UserEntityReq(

        @NotNull(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        Optional<@Positive(message = "{wiki.chiu.micro.user.valid.UserSave.message}") Long> id,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        @Pattern(regexp = USERNAME_REGEX, message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        String username,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        String nickname,

        @NotNull(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        @Pattern(regexp = URL_REGEX, message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        String avatar,

        String password,

        @NotNull(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        @Pattern(regexp = EMAIL_REGEX, message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        String email,

        @NotNull(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        @Pattern(regexp = PHONE_REGEX, message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        String phone,

        @NotNull(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        @Min(value = 0, message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        @Max(value = 1, message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        Integer status,

        @NotEmpty(message = "{wiki.chiu.micro.user.valid.UserSave.message}")
        List<@NotBlank(message = "{wiki.chiu.micro.user.valid.UserSave.message}") String> roles) {

        public UserEntityReq(UserEntityReq req, String password) {
                this(req.id, req.username, req.nickname, req.avatar, password, req.email, req.phone, req.status, req.roles);
        }

        public UserEntityReq(UserEntityRegisterReq req, Long id, Integer status, List<String> roles) {
                this(Optional.ofNullable(id), req.username(), req.nickname(), req.avatar(), req.password(), req.email(), req.phone(), status, roles);
        }
}
