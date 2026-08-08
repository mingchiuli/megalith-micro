package wiki.chiu.micro.common.web;


import java.util.Collection;

public final class ValidatedRequest {

    // ── Number validation ──

    public <T extends Number> T positive(T value, String name) {
        if (value == null || value.longValue() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    public <T extends Number> T nonNegative(T value, String name) {
        if (value == null || value.longValue() < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    public <T extends Number> T range(T value, long min, long max, String name) {
        if (value == null) {
            return null;
        }
        if (value.longValue() < min || value.longValue() > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }

    // ── String validation ──

    public String notBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public String maxLength(String value, int max, String name) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(name + " size must be <= " + max);
        }
        return value;
    }

    public String size(String value, int min, int max, String name) {
        if (value == null || value.length() < min || value.length() > max) {
            throw new IllegalArgumentException(name + " size must be between " + min + " and " + max);
        }
        return value;
    }

    // ── General ──

    public <T> T notNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }

    // ── Collection validation ──

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
}
