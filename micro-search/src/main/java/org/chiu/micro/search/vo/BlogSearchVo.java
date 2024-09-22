package org.chiu.micro.search.vo;

import java.util.List;

public record BlogSearchVo(

        Long total,

        Integer currentPage,

        Integer size,

        List<Long> ids) {

    public static BlogSearchVoBuilder builder() {
        return new BlogSearchVoBuilder();
    }

    public static class BlogSearchVoBuilder {
        private Long total;
        private Integer currentPage;
        private Integer size;
        private List<Long> ids;

        public BlogSearchVoBuilder total(Long total) {
            this.total = total;
            return this;
        }

        public BlogSearchVoBuilder currentPage(Integer currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public BlogSearchVoBuilder size(Integer size) {
            this.size = size;
            return this;
        }

        public BlogSearchVoBuilder ids(List<Long> ids) {
            this.ids = ids;
            return this;
        }

        public BlogSearchVo build() {
            return new BlogSearchVo(total, currentPage, size, ids);
        }

    }
}
