package wiki.chiu.micro.common.req;



import java.io.Serializable;
import java.time.LocalDateTime;

public record BlogSysSearchReq(

        Integer page,

        Integer pageSize,

        String keywords,

        LocalDateTime createStart,

        LocalDateTime createEnd) implements Serializable {

    public static BlogSysSearchReq.BlogSearchReqBuilder builder() {
        return new BlogSysSearchReq.BlogSearchReqBuilder();
    }

    public static class BlogSearchReqBuilder {
        private Integer page;
        private Integer pageSize;
        private String keywords;
        private LocalDateTime createStart;
        private LocalDateTime createEnd;

        public BlogSysSearchReq.BlogSearchReqBuilder page(Integer page) {
            this.page = page;
            return this;
        }

        public BlogSysSearchReq.BlogSearchReqBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public BlogSysSearchReq.BlogSearchReqBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public BlogSysSearchReq.BlogSearchReqBuilder createStart(LocalDateTime createStart) {
            this.createStart = createStart;
            return this;
        }

        public BlogSysSearchReq.BlogSearchReqBuilder createEnd(LocalDateTime createEnd) {
            this.createEnd = createEnd;
            return this;
        }

        public BlogSysSearchReq build() {
            return new BlogSysSearchReq(page, pageSize, keywords, createStart, createEnd);
        }
    }
}
