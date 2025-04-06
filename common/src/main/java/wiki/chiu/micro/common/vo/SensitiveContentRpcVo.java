package wiki.chiu.micro.common.vo;

import java.io.Serializable;

public record SensitiveContentRpcVo(

        Integer startIndex,

        Integer endIndex,

        Integer type) implements Serializable {

    public static SensitiveContentRpcVoBuilder builder() {
        return new SensitiveContentRpcVoBuilder(null, null, null);
    }

    public record SensitiveContentRpcVoBuilder(
            Integer startIndex,
            Integer endIndex,
            Integer type
    ) {

        public SensitiveContentRpcVoBuilder startIndex(Integer startIndex) {
            return new SensitiveContentRpcVoBuilder(startIndex, this.endIndex, this.type);
        }

        public SensitiveContentRpcVoBuilder endIndex(Integer endIndex) {
            return new SensitiveContentRpcVoBuilder(this.startIndex, endIndex, this.type);
        }

        public SensitiveContentRpcVoBuilder type(Integer type) {
            return new SensitiveContentRpcVoBuilder(this.startIndex, this.endIndex, type);
        }

        public SensitiveContentRpcVo build() {
            return new SensitiveContentRpcVo(startIndex, endIndex, type);
        }
    }
}