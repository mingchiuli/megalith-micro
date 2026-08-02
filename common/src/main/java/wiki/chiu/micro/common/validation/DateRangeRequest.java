package wiki.chiu.micro.common.validation;

import java.time.LocalDateTime;

public interface DateRangeRequest {

    LocalDateTime createStart();

    LocalDateTime createEnd();

    default boolean hasValidDateRange() {
        LocalDateTime start = createStart();
        LocalDateTime end = createEnd();
        return start == null ? end == null : end != null && start.isBefore(end);
    }
}
