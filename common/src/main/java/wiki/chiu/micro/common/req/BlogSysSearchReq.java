package wiki.chiu.micro.common.req;

import java.time.LocalDateTime;
import java.util.Collections;
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

    public static BlogSysSearchReqBuilder builder() {
        return new BlogSysSearchReqBuilder(null, null, null, null, null, null, null, Collections.emptyList());
    }

    public record BlogSysSearchReqBuilder(
            Integer page,
            Integer pageSize,
            String keywords,
            Integer status,
            LocalDateTime createStart,
            LocalDateTime createEnd,
            Long userId,
            List<String> roles) {

        public BlogSysSearchReqBuilder page(Integer page) {
            return new BlogSysSearchReqBuilder(page, this.pageSize, this.keywords, this.status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysSearchReqBuilder pageSize(Integer pageSize) {
            return new BlogSysSearchReqBuilder(this.page, pageSize, this.keywords, this.status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysSearchReqBuilder keywords(String keywords) {
            return new BlogSysSearchReqBuilder(this.page, this.pageSize, keywords, this.status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysSearchReqBuilder status(Integer status) {
            return new BlogSysSearchReqBuilder(this.page, this.pageSize, this.keywords, status, this.createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysSearchReqBuilder createStart(LocalDateTime createStart) {
            return new BlogSysSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, createStart, this.createEnd, this.userId, this.roles);
        }

        public BlogSysSearchReqBuilder createEnd(LocalDateTime createEnd) {
            return new BlogSysSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, this.createStart, createEnd, this.userId, this.roles);
        }

        public BlogSysSearchReqBuilder userId(Long userId) {
            return new BlogSysSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, this.createStart, this.createEnd, userId, this.roles);
        }

        public BlogSysSearchReqBuilder roles(List<String> roles) {
            return new BlogSysSearchReqBuilder(this.page, this.pageSize, this.keywords, this.status, this.createStart, this.createEnd, this.userId, roles);
        }

        public BlogSysSearchReq build() {
            return new BlogSysSearchReq(page, pageSize, keywords, status, createStart, createEnd, userId, roles);
        }
    }
}