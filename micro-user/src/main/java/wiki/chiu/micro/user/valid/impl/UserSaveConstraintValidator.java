package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.Const;

import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.valid.UserSave;

import java.util.Arrays;
import java.util.List;

import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

public class UserSaveConstraintValidator implements ConstraintValidator<UserSave, UserEntityReq> {

    private final List<Integer> STATUS_LIST = Arrays.stream(StatusEnum.values())
            .map(StatusEnum::getCode)
            .toList();

    @Override
    public boolean isValid(UserEntityReq req, ConstraintValidatorContext context) {
        if (!isUsername(req.username()) || !StringUtils.hasLength(req.nickname()) || !Const.URL_PATTERN.matcher(req.avatar()).matches() || !Const.EMAIL_PATTERN.matcher(req.email()).matches() || !Const.PHONE_PATTERN.matcher(req.phone()).matches() || CollectionUtils.isEmpty(req.roles())) {
            return false;
        }

        if (req.id().isEmpty() && !StringUtils.hasLength(req.password())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PASSWORD_REQUIRED.getMsg()).addConstraintViolation();
            return false;
        }

        return STATUS_LIST.contains(req.status());
    }

    private boolean isUsername(String username) {
        return StringUtils.hasLength(username) && !isPhoneNumber(username) && !isEmail(username);
    }

    private boolean isPhoneNumber(String value) {
        return Const.PHONE_PATTERN.matcher(value).matches();
    }

    private boolean isEmail(String value) {
        return Const.EMAIL_PATTERN.matcher(value).matches();
    }
}
