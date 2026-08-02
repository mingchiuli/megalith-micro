package wiki.chiu.micro.blog.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import wiki.chiu.micro.common.validation.DateRangeRequest;
import wiki.chiu.micro.common.validation.ValidDateRange;

@ValidDateRange(message = "{wiki.chiu.micro.blog.valid.BlogDownload.message}")
public record BlogDownloadReq(

        @Size(max = 20, message = "{wiki.chiu.micro.blog.valid.BlogDownload.message}")
        String keywords,

        @Min(value = 0, message = "{wiki.chiu.micro.blog.valid.BlogDownload.message}")
        @Max(value = 3, message = "{wiki.chiu.micro.blog.valid.BlogDownload.message}")
        Integer status,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createStart,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createEnd) implements DateRangeRequest {
}
