package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.user.valid.Phone;

import java.util.regex.Pattern;


/**
 * @author mingchiuli
 * @create 2023-03-08 1:26 am
 */
public class PhoneConstraintValidator implements ConstraintValidator<Phone, String> {

    private static final Pattern PATTERN = Pattern.compile("^1[3-9]\\d{9}$");


    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (Boolean.FALSE.equals(StringUtils.hasLength(phone))) {
            return true;
        }
        return PATTERN.matcher(phone).matches();
    }
}
