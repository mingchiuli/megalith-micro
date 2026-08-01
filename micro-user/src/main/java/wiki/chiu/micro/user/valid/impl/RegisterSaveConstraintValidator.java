package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.valid.RegisterSave;

import java.util.Objects;

import static wiki.chiu.micro.common.lang.ExceptionMessage.PASSWORD_DIFF;

public class RegisterSaveConstraintValidator implements ConstraintValidator<RegisterSave, UserEntityRegisterReq> {

    @Override
    public boolean isValid(UserEntityRegisterReq req, ConstraintValidatorContext context) {
        if (req == null || !StringUtils.hasLength(req.token()) || !isValidNickname(req.nickname()) ||
                !isValidEmail(req.email()) || !isValidUsername(req.username())) {
            return false;
        }
        return isPasswordConfirmed(req.password(), req.confirmPassword(), context);
    }

    private boolean isValidNickname(String nickname) {
        return StringUtils.hasLength(nickname);
    }

    private boolean isValidEmail(String email) {
        return email != null && Const.EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidUsername(String username) {
        return StringUtils.hasLength(username) &&
                !Const.PHONE_PATTERN.matcher(username).matches() &&
                !Const.EMAIL_PATTERN.matcher(username).matches();
    }

    private boolean isPasswordConfirmed(String password, String confirmPassword, ConstraintValidatorContext context) {
        if (!Objects.equals(confirmPassword, password)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PASSWORD_DIFF.getMsg()).addConstraintViolation();
            return false;
        }
        return true;
    }

}
