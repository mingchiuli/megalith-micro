package wiki.chiu.micro.common.req;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record BlogSysCountSearchReq(
        Integer page,
        Integer pageSize,
        String keywords,
        Integer status,
        LocalDateTime createStart,
        LocalDateTime createEnd,
        Long userId,
        List<String> roles) {

    public static BlogSysCountSearchReqBuilder builder() {
        return new BlogSysCountSearchReqBuilder(null, null, null, null, null, null, null, Collections.emptyList());
    }

    public record BlogSysCountSearchReqBuilder(
            Integer page,
            Integer pageSize,
            String keywords,
            Integer status,
            LocalDateTime createStart,
            LocalDateTime createEnd,
            Long userId,
            List<String> roles) {

        public BlogSysCountSearchReqBuilder page(Integer page) {
            return new BlogSysCountSearchReqBuilder(page, this.pageSize, this.keywords, this.status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder pageSize(Integer pageSize) {
            return new BlogSysCountSearchReqBuilder(this.page, pageSize, this.keywords, this.status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder keywords(String keywords) {
            return new BlogSysCountSearchReqBuilder(this.page, this.pageSize, keywords, this.status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder status(Integer status) {
            return new BlogSysCountSearchReqBuilder(this.page, this.pageSize, this.keywords, status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder createStart(LocalDateTime createStart) {
            return new BlogSysCountSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder createEnd(LocalDateTime createEnd) {
            return new BlogSysCountSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, this.createStart, createEnd, this.userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder userId(Long userId) {
            return new BlogSysCountSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, this.createStart, this.createEnd, userId, this.roles);
        }

        public BlogSysCountSearchReqBuilder roles(List<String> roles) {
            return new BlogSysCountSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, this.createStart, this.createEnd, this.userId, roles);
        }

        public BlogSysCountSearchReq build() {
            return new BlogSysCountSearchReq(page, pageSize, keywords, status, createStart, createEnd, userId, roles);
        }
    }
}