package wiki.chiu.micro.blog.req;

public record SensitiveContentReq(

        Integer startIndex,

        Integer endIndex,

        Integer type) {

    public static SensitiveContentReqBuilder builder() {
        return new SensitiveContentReqBuilder();
    }

    public static class SensitiveContentReqBuilder {
        private Integer startIndex;
        private Integer endIndex;
        private Integer type;

        public SensitiveContentReqBuilder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public SensitiveContentReqBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public SensitiveContentReqBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public SensitiveContentReq build() {
            return new SensitiveContentReq(this.startIndex, this.endIndex, this.type);
        }
    }
}
