package wiki.chiu.micro.blog.req;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public record BlogQueryReq(

        @NotNull
        Integer currentPage,

        @NotNull
        Integer size,

        @Size(max = 20)
        String keywords,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createStart,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createEnd) implements Serializable {
}
