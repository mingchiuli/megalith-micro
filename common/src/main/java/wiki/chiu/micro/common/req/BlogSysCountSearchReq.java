package wiki.chiu.micro.common.req;



import java.time.LocalDateTime;
import java.util.List;

public record BlogSysCountSearchReq(

        String keywords,

        Integer status,

        LocalDateTime createStart,

        LocalDateTime createEnd,

        Long userId,

        List<String> roles
) {

    public static BlogSysCountSearchReq.BLogSysCountSearchReqBuilder builder() {
        return new BlogSysCountSearchReq.BLogSysCountSearchReqBuilder();
    }

    public static class BLogSysCountSearchReqBuilder {

        private String keywords;

        private Integer status;

        private LocalDateTime createStart;

        private LocalDateTime createEnd;

        private Long userId;

        private List<String> roles;

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder status(Integer status) {
            this.status = status;
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

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public BlogSysCountSearchReq build() {
            return new BlogSysCountSearchReq(keywords, status, createStart, createEnd, userId, roles);
        }
    }
}
