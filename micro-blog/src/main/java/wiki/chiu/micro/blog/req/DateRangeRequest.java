package wiki.chiu.micro.blog.req;

import java.time.LocalDateTime;

public interface DateRangeRequest {

    LocalDateTime createStart();

    LocalDateTime createEnd();
}
