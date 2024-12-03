package wiki.chiu.micro.common.req;



import java.time.LocalDateTime;

public record BlogSysCountSearchReq(

        String keywords,

        LocalDateTime createStart,

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