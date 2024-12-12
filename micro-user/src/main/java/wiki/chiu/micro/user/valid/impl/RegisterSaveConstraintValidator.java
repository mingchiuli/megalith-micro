package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.valid.RegisterSave;

import java.util.Objects;
import java.util.regex.Pattern;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

public class RegisterSaveConstraintValidator implements ConstraintValidator<RegisterSave, UserEntityRegisterReq> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$");

    private final StringRedisTemplate redisTemplate;

    public RegisterSaveConstraintValidator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isValid(UserEntityRegisterReq req, ConstraintValidatorContext context) {
        String token = req.token();
        if (!StringUtils.hasLength(token)) {
            return false;
        }

        String nickname = req.nickname();
        if (!StringUtils.hasLength(nickname)) {
            return false;
        }

        String email = req.email();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }

        String username = req.username();
        if (PHONE_PATTERN.matcher(username).matches() || EMAIL_PATTERN.matcher(username).matches()) {
            return false;
        }

        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX + token);

        context.disableDefaultConstraintViolation();

        if (Boolean.FALSE.equals(exist)) {
            context.buildConstraintViolationWithTemplate(NO_AUTH.getMsg()).addConstraintViolation();
            return false;
        }
        String password = req.password();
        String confirmPassword = req.confirmPassword();
        if (!Objects.equals(confirmPassword, password)) {
            context.buildConstraintViolationWithTemplate(PASSWORD_DIFF.getMsg()).addConstraintViolation();
            return false;
        }

        String usernameCopy = redisTemplate.opsForValue().get(REGISTER_PREFIX + token);
        if (StringUtils.hasLength(usernameCopy) && !Objects.equals(usernameCopy, username)) {
            context.buildConstraintViolationWithTemplate(NO_AUTH.getMsg()).addConstraintViolation();
            return false;
        }

        return true;
    }
}
