package wiki.chiu.micro.common.validation;

import java.time.LocalDateTime;

public interface DateRangeRequest {

    LocalDateTime createStart();

    LocalDateTime createEnd();
}
