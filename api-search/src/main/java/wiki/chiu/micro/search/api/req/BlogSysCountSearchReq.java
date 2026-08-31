package wiki.chiu.micro.search.api.req;

import java.time.LocalDateTime;

public record BlogSysCountSearchReq(
    String keywords,
    Integer status,
    LocalDateTime createStart,
    LocalDateTime createEnd,
    Long userId,
    Boolean allData) {

    public static BlogSysCountSearchReq.BLogSysCountSearchReqBuilder builder() {
        return new BlogSysCountSearchReq.BLogSysCountSearchReqBuilder();
    }

    public static class BLogSysCountSearchReqBuilder {

        private String keywords;

        private Integer status;

        private LocalDateTime createStart;

        private LocalDateTime createEnd;

        private Long userId;

        private Boolean allData;

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder createStart(
            LocalDateTime createStart) {
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

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder allData(Boolean allData) {
            this.allData = allData;
            return this;
        }

        public BlogSysCountSearchReq build() {
            return new BlogSysCountSearchReq(keywords, status, createStart, createEnd, userId, allData);
        }
    }
}
