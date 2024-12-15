package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.user.valid.Username;



/**
 * @author mingchiuli
 * @create 2023-03-08 1:11 am
 */
public class UsernameConstraintValidator implements ConstraintValidator<Username, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (StringUtils.hasLength(username)) {
            return Boolean.FALSE.equals(Const.PHONE_PATTERN.matcher(username).matches()) && Boolean.FALSE.equals(Const.EMAIL_PATTERN.matcher(username).matches());
        }
        return false;
    }
}

