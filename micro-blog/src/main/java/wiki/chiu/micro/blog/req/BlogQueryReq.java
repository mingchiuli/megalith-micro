package wiki.chiu.micro.blog.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BlogQueryReq(

        @NotNull
        Integer currentPage,

        @NotNull
        Integer size,

        @Size(max = 20)
        String keywords,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createStart,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createEnd) {
}
