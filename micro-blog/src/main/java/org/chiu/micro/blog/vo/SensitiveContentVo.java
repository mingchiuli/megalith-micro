package org.chiu.micro.blog.vo;


public record SensitiveContentVo(

        Integer startIndex,

        Integer endIndex,

        Integer type) {

    public static SensitiveContentVoBuilder builder() {
        return new SensitiveContentVoBuilder();
    }

    public static class SensitiveContentVoBuilder {
        private Integer startIndex;
        private Integer endIndex;
        private Integer type;


        public SensitiveContentVoBuilder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public SensitiveContentVoBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public SensitiveContentVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public SensitiveContentVo build() {
            return new SensitiveContentVo(startIndex, endIndex, type);
        }
    }
}
