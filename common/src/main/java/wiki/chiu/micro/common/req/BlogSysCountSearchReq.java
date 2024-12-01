package wiki.chiu.micro.common.req;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BlogSysCountSearchReq(

        String keywords,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        LocalDateTime createStart,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        LocalDateTime createEnd) {

    public static BlogSysCountSearchReq.BLogSysCountSearchReqBuilder builder() {
        return new BlogSysCountSearchReq.BLogSysCountSearchReqBuilder();
    }

    public static class BLogSysCountSearchReqBuilder {

        private String keywords;

        private LocalDateTime createStart;

        private LocalDateTime createEnd;

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder createStart(LocalDateTime createStart) {
            this.createStart = createStart;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder createEnd(LocalDateTime createEnd) {
            this.createEnd = createEnd;
            return this;
        }

        public BlogSysCountSearchReq build() {
            return new BlogSysCountSearchReq(keywords, createStart, createEnd);
        }
    }
}
