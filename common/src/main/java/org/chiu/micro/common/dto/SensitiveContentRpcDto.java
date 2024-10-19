package org.chiu.micro.common.dto;

import java.io.Serializable;

public record SensitiveContentRpcDto(

        Integer startIndex,

        Integer endIndex,

        Integer type) implements Serializable {

    public static SensitiveContentBuilder builder() {
        return new SensitiveContentBuilder();
    }

    public static class SensitiveContentBuilder {
        private Integer startIndex;
        private Integer endIndex;
        private Integer type;


        public SensitiveContentBuilder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public SensitiveContentBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public SensitiveContentBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public SensitiveContentRpcDto build() {
            return new SensitiveContentRpcDto(startIndex, endIndex, type);
        }
    }
}
