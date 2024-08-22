package org.chiu.micro.user.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;


/**
 * @author mingchiuli
 * @create 2023-03-08 1:11 am
 */
public class UsernameConstraintValidator implements ConstraintValidator<Username, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (StringUtils.hasLength(username)) {
            return Boolean.FALSE.equals(username.matches("\\d+")) && Boolean.FALSE.equals(username.contains("@"));
        }
        return false;
    }
}

