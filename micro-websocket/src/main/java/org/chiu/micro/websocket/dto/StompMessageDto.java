package org.chiu.micro.websocket.dto;

import java.io.Serializable;

public class StompMessageDto implements Serializable {

    private Integer version;

    private Long userId;

    private Long blogId;

    private Integer type;

    StompMessageDto(Integer version, Long userId, Long blogId, Integer type) {
        this.version = version;
        this.userId = userId;
        this.blogId = blogId;
        this.type = type;
    }

    public static StompMessageDtoBuilder builder() {
        return new StompMessageDtoBuilder();
    }

    public Integer getVersion() {
        return this.version;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getBlogId() {
        return this.blogId;
    }

    public Integer getType() {
        return this.type;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof StompMessageDto)) return false;
        final StompMessageDto other = (StompMessageDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$blogId = this.getBlogId();
        final Object other$blogId = other.getBlogId();
        if (this$blogId == null ? other$blogId != null : !this$blogId.equals(other$blogId)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StompMessageDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $blogId = this.getBlogId();
        result = result * PRIME + ($blogId == null ? 43 : $blogId.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "StompMessageDto(version=" + this.getVersion() + ", userId=" + this.getUserId() + ", blogId=" + this.getBlogId() + ", type=" + this.getType() + ")";
    }

    public static class StompMessageDtoBuilder {
        private Integer version;
        private Long userId;
        private Long blogId;
        private Integer type;

        StompMessageDtoBuilder() {
        }

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
            return new StompMessageDto(this.version, this.userId, this.blogId, this.type);
        }

        public String toString() {
            return "StompMessageDto.StompMessageDtoBuilder(version=" + this.version + ", userId=" + this.userId + ", blogId=" + this.blogId + ", type=" + this.type + ")";
        }
    }
}
