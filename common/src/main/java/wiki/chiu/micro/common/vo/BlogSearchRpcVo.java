package wiki.chiu.micro.common.vo;

import java.util.Collections;
import java.util.List;

public record BlogSearchRpcVo(
        Long total,
        Integer currentPage,
        Integer size,
        List<Long> ids) {

    public static BlogSearchRpcVoBuilder builder() {
        return new BlogSearchRpcVoBuilder(null, null, null, Collections.emptyList());
    }

    public record BlogSearchRpcVoBuilder(
            Long total,
            Integer currentPage,
            Integer size,
            List<Long> ids
    ) {

        public BlogSearchRpcVoBuilder total(Long total) {
            return new BlogSearchRpcVoBuilder(total, this.currentPage, this.size, this.ids);
        }

        public BlogSearchRpcVoBuilder currentPage(Integer currentPage) {
            return new BlogSearchRpcVoBuilder(this.total, currentPage, this.size, this.ids);
        }

        public BlogSearchRpcVoBuilder size(Integer size) {
            return new BlogSearchRpcVoBuilder(this.total, this.currentPage, size, this.ids);
        }

        public BlogSearchRpcVoBuilder ids(List<Long> ids) {
            return new BlogSearchRpcVoBuilder(this.total, this.currentPage, this.size, ids);
        }

        public BlogSearchRpcVo build() {
            return new BlogSearchRpcVo(total, currentPage, size, ids);
        }
    }
}