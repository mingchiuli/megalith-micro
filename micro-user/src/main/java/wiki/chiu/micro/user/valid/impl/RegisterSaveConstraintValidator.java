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
        String token = req.token();
        if (!StringUtils.hasLength(token)) {
            return false;
        }

        String nickname = req.nickname();
        if (!StringUtils.hasLength(nickname)) {
            return false;
        }

        String email = req.email();
        if (!Const.EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }

        String username = req.username();
        if (StringUtils.hasLength(username) || Const.PHONE_PATTERN.matcher(username).matches() || Const.EMAIL_PATTERN.matcher(username).matches()) {
            return false;
        }

        Optional<UserEntity> userEntityOptional = userRepository.findByUsername(username);
        if (userEntityOptional.isPresent()) {
            if (StatusEnum.HIDE.getCode().equals(userEntityOptional.get().getStatus())) {
                return false;
            }
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
