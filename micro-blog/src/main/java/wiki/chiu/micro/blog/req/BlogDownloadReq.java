package wiki.chiu.micro.blog.req;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record BlogDownloadReq(
    String keywords,
    Integer status,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createStart,
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createEnd) {}
