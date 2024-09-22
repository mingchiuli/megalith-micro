package org.chiu.micro.websocket.dto;

import java.io.Serializable;

public record StompMessageDto(

        Integer version,

        Long userId,

        Long blogId,

        Integer type) implements Serializable {


    public static StompMessageDtoBuilder builder() {
        return new StompMessageDtoBuilder();
    }

    public static class StompMessageDtoBuilder {
        private Integer version;
        private Long userId;
        private Long blogId;
        private Integer type;

        public StompMessageDtoBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public StompMessageDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public StompMessageDtoBuilder blogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public StompMessageDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public StompMessageDto build() {
            return new StompMessageDto(version, userId, blogId, type);
        }
    }
}
