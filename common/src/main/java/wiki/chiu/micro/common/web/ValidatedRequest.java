package wiki.chiu.micro.common.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Collection;
import java.util.Set;

public final class ValidatedRequest {

    private final Validator validator;

    public ValidatedRequest(Validator validator) {
        this.validator = validator;
    }

    public <T> T body(ServerRequest request, Class<T> type, Class<?>... groups) throws Exception {
        return validate(request.body(type), groups);
    }

    public <T> T body(ServerRequest request, ParameterizedTypeReference<T> type) throws Exception {
        return validate(request.body(type));
    }

    public <T> T validate(T value, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(value, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return value;
    }

    public <T extends Collection<?>> T notEmpty(T value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value;
    }

    public <T extends Collection<? extends Number>> T positiveElements(T value, String name) {
        if (value == null || value.stream().anyMatch(item -> item == null || item.longValue() <= 0)) {
            throw new IllegalArgumentException(name + " must contain only positive values");
        }
        return value;
    }

    public <T extends Collection<String>> T notBlankElements(T value, String name) {
        if (value == null || value.stream().anyMatch(item -> item == null || item.isBlank())) {
            throw new IllegalArgumentException(name + " must contain only non-blank values");
        }
        return value;
    }

    public String notBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public String size(String value, int min, int max, String name) {
        if (value == null || value.length() < min || value.length() > max) {
            throw new IllegalArgumentException(
                    name + " size must be between " + min + " and " + max);
        }
        return value;
    }
}
