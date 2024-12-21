package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.valid.RegisterSave;

import java.util.Objects;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

public class RegisterSaveConstraintValidator implements ConstraintValidator<RegisterSave, UserEntityRegisterReq> {

    private final StringRedisTemplate redisTemplate;

    private final UserRepository userRepository;

    public RegisterSaveConstraintValidator(StringRedisTemplate redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(UserEntityRegisterReq req, ConstraintValidatorContext context) {
        if (!isValidToken(req.token(), context) || !isValidNickname(req.nickname()) || !isValidEmail(req.email()) || !isValidUsername(req.username(), context)) {
            return false;
        }

        if (!isPasswordConfirmed(req.password(), req.confirmPassword(), context)) {
            return false;
        }

        return isUsernameAuthorized(req.token(), req.username(), context);
    }

    private boolean isValidToken(String token, ConstraintValidatorContext context) {
        if (!StringUtils.hasLength(token)) {
            return false;
        }

        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX + token);
        if (Boolean.FALSE.equals(exist)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(NO_AUTH.getMsg()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isValidNickname(String nickname) {
        return StringUtils.hasLength(nickname);
    }

    private boolean isValidEmail(String email) {
        return Const.EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidUsername(String username, ConstraintValidatorContext context) {
        if (!StringUtils.hasLength(username) || Const.PHONE_PATTERN.matcher(username).matches() || Const.EMAIL_PATTERN.matcher(username).matches()) {
            return false;
        }

        Optional<UserEntity> userEntityOptional = userRepository.findByUsername(username);
        if (userEntityOptional.isPresent() && StatusEnum.HIDE.getCode().equals(userEntityOptional.get().getStatus())) {
            return false;
        }
        return true;
    }

    private boolean isPasswordConfirmed(String password, String confirmPassword, ConstraintValidatorContext context) {
        if (!Objects.equals(confirmPassword, password)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PASSWORD_DIFF.getMsg()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isUsernameAuthorized(String token, String username, ConstraintValidatorContext context) {
        String usernameCopy = redisTemplate.opsForValue().get(REGISTER_PREFIX + token);
        if (StringUtils.hasLength(usernameCopy) && !Objects.equals(usernameCopy, username)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(NO_AUTH.getMsg()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
