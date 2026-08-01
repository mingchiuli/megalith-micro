package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.valid.PasswordMatches;

import java.util.Objects;

public class PasswordMatchesConstraintValidator implements ConstraintValidator<PasswordMatches, UserEntityRegisterReq> {

    @Override
    public boolean isValid(UserEntityRegisterReq request, ConstraintValidatorContext context) {
        return request == null || Objects.equals(request.password(), request.confirmPassword());
    }
}
