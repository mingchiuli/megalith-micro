package wiki.chiu.micro.common.vo;

import java.util.List;

public record BlogSearchRpcVo(

        Long total,

        Integer currentPage,

        Integer size,

        List<Long> ids) {

    public static BlogSearchRpcVo.BlogSearchRpcVoBuilder builder() {
        return new BlogSearchRpcVo.BlogSearchRpcVoBuilder();
    }

    public static class BlogSearchRpcVoBuilder {
        private Long total;

        private Integer currentPage;

        private Integer size;

        private List<Long> ids;

        public BlogSearchRpcVo.BlogSearchRpcVoBuilder total(Long total) {
            this.total = total;
            return this;
        }

        public BlogSearchRpcVo.BlogSearchRpcVoBuilder currentPage(Integer currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public BlogSearchRpcVo.BlogSearchRpcVoBuilder size(Integer size) {
            this.size = size;
            return this;
        }

        public BlogSearchRpcVo.BlogSearchRpcVoBuilder ids(List<Long> ids) {
            this.ids = ids;
            return this;
        }


        public BlogSearchRpcVo build() {
            return new BlogSearchRpcVo(total, currentPage, size, ids);
        }
    }

}
