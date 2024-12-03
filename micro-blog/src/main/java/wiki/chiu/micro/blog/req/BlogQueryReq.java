package wiki.chiu.micro.blog.req;


import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public record BlogQueryReq(

        Integer currentPage,

        Integer size,

        String keywords,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createStart,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createEnd) implements Serializable {
}
