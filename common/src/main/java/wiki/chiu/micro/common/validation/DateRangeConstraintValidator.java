package wiki.chiu.micro.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateRangeConstraintValidator implements ConstraintValidator<ValidDateRange, DateRangeRequest> {

    @Override
    public boolean isValid(DateRangeRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        LocalDateTime start = request.createStart();
        LocalDateTime end = request.createEnd();
        return start == null ? end == null : end != null && start.isBefore(end);
    }
}
