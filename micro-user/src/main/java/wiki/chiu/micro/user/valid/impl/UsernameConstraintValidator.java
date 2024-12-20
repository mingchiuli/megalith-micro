package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.user.valid.Username;



/**
 * Validates that a username is not a phone number or email.
 * @author mingchiuli
 * @create 2023-03-08 1:11 am
 */
public class UsernameConstraintValidator implements ConstraintValidator<Username, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return StringUtils.hasLength(username) && !isPhoneNumber(username) && !isEmail(username);
    }

    private boolean isPhoneNumber(String value) {
        return Const.PHONE_PATTERN.matcher(value).matches();
    }

    private boolean isEmail(String value) {
        return Const.EMAIL_PATTERN.matcher(value).matches();
    }
}

