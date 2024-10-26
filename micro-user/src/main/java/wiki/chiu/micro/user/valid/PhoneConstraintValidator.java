package wiki.chiu.micro.user.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;


/**
 * @author mingchiuli
 * @create 2023-03-08 1:26 am
 */
public class PhoneConstraintValidator implements ConstraintValidator<Phone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (Boolean.FALSE.equals(StringUtils.hasLength(phone))) {
            return true;
        }
        return phone.matches("\\d+");
    }
}
