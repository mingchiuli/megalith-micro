package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.user.valid.Phone;



/**
 * @author mingchiuli
 * @create 2023-03-08 1:26 am
 */
public class PhoneConstraintValidator implements ConstraintValidator<Phone, String> {


    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (!StringUtils.hasLength(phone)) {
            return true;
        }
        return Const.PHONE_PATTERN.matcher(phone).matches();
    }
}
