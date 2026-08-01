package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import wiki.chiu.micro.blog.req.DateRangeRequest;

import java.time.LocalDateTime;

public class DateRangeConstraintValidator implements ConstraintValidator<wiki.chiu.micro.blog.valid.ValidDateRange, DateRangeRequest> {

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
