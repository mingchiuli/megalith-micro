package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.valid.PasswordRequiredForCreate;

public class PasswordRequiredForCreateConstraintValidator
        implements ConstraintValidator<PasswordRequiredForCreate, UserEntityReq> {

    @Override
    public boolean isValid(UserEntityReq request, ConstraintValidatorContext context) {
        return request == null || request.id() == null || request.id().isPresent()
                || StringUtils.hasLength(request.password());
    }
}
