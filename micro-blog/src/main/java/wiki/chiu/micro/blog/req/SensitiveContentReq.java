package wiki.chiu.micro.blog.req;

public record SensitiveContentReq(

        Integer startIndex,

        Integer endIndex,

        Integer type) {

    public static SensitiveContentReqBuilder builder() {
        return new SensitiveContentReqBuilder(null, null, null);
    }

    public record SensitiveContentReqBuilder(
            Integer startIndex,
            Integer endIndex,
            Integer type
    ) {

        public SensitiveContentReqBuilder startIndex(Integer startIndex) {
            return new SensitiveContentReqBuilder(startIndex, endIndex, type);
        }

        public SensitiveContentReqBuilder endIndex(Integer endIndex) {
            return new SensitiveContentReqBuilder(startIndex, endIndex, type);
        }

        public SensitiveContentReqBuilder type(Integer type) {
            return new SensitiveContentReqBuilder(startIndex, endIndex, type);
        }

        public SensitiveContentReq build() {
            return new SensitiveContentReq(this.startIndex, this.endIndex, this.type);
        }
    }
}
