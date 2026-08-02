package wiki.chiu.micro.blog.req;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import wiki.chiu.micro.common.validation.DateRangeRequest;

public record BlogQueryReq(

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        @Min(value = 1, message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        Integer currentPage,

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        @Min(value = 1, message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        Integer size,

        @Size(max = 20, message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        String keywords,

        @Min(value = 0, message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        @Max(value = 3, message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
        Integer status,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createStart,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createEnd) implements DateRangeRequest {

    @JsonIgnore
    @AssertTrue(message = "{wiki.chiu.micro.blog.valid.BlogQuery.message}")
    public boolean isDateRangeValid() {
        return hasValidDateRange();
    }
}
