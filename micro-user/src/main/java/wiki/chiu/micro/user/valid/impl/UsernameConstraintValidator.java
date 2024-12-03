package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.user.valid.Username;

import java.util.regex.Pattern;


/**
 * @author mingchiuli
 * @create 2023-03-08 1:11 am
 */
public class UsernameConstraintValidator implements ConstraintValidator<Username, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$");


    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (StringUtils.hasLength(username)) {
            return Boolean.FALSE.equals(PHONE_PATTERN.matcher(username).matches()) && Boolean.FALSE.equals(EMAIL_PATTERN.matcher(username).matches());
        }
        return false;
    }
}

