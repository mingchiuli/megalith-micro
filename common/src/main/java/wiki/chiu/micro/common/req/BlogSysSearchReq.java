package wiki.chiu.micro.common.req;



import java.time.LocalDateTime;
import java.util.List;

public record BlogSysSearchReq(

        Integer page,

        Integer pageSize,

        String keywords,

        Integer status,

        LocalDateTime createStart,

        LocalDateTime createEnd,

        Long userId,

        List<String> roles) {

    public static BlogSysSearchReq.BlogSearchReqBuilder builder() {
        return new BlogSysSearchReq.BlogSearchReqBuilder();
    }

    public static class BlogSearchReqBuilder {
        private Integer page;
        private Integer pageSize;
        private String keywords;
        private Integer status;
        private LocalDateTime createStart;
        private LocalDateTime createEnd;
        private Long userId;
        private List<String> roles;

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

        public BlogSysSearchReq.BlogSearchReqBuilder status(Integer status) {
            this.status = status;
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

        public BlogSysSearchReq.BlogSearchReqBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogSysSearchReq.BlogSearchReqBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public BlogSysSearchReq build() {
            return new BlogSysSearchReq(page, pageSize, keywords, status, createStart, createEnd, userId, roles);
        }
    }
}
