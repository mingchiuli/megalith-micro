package wiki.chiu.micro.common.req;

public record BlogSearchReq(
        Integer page,
        Integer pageSize,
        String keywords) {

    public static BlogSearchReq.BlogSearchReqBuilder builder() {
        return new BlogSearchReq.BlogSearchReqBuilder();
    }

    public static class BlogSearchReqBuilder {
        private Integer page;
        private Integer pageSize;
        private String keywords;

        public BlogSearchReq.BlogSearchReqBuilder page(Integer page) {
            this.page = page;
            return this;
        }

        public BlogSearchReq.BlogSearchReqBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public BlogSearchReq.BlogSearchReqBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public BlogSearchReq build() {
            return new BlogSearchReq(page, pageSize, keywords);
        }
    }
}
