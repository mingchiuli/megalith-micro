package org.chiu.micro.search.constant;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2021-12-13 10:46 AM
 */
public class BlogOperateMessage implements Serializable {

    private Long blogId;

    private BlogOperateEnum typeEnum;

    private Integer year;

    public BlogOperateMessage(Long blogId, BlogOperateEnum typeEnum, Integer year) {
        this.blogId = blogId;
        this.typeEnum = typeEnum;
        this.year = year;
    }

    public Long getBlogId() {
        return this.blogId;
    }

    public BlogOperateEnum getTypeEnum() {
        return this.typeEnum;
    }

    public Integer getYear() {
        return this.year;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public void setTypeEnum(BlogOperateEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogOperateMessage)) return false;
        final BlogOperateMessage other = (BlogOperateMessage) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$blogId = this.getBlogId();
        final Object other$blogId = other.getBlogId();
        if (this$blogId == null ? other$blogId != null : !this$blogId.equals(other$blogId)) return false;
        final Object this$typeEnum = this.getTypeEnum();
        final Object other$typeEnum = other.getTypeEnum();
        if (this$typeEnum == null ? other$typeEnum != null : !this$typeEnum.equals(other$typeEnum)) return false;
        final Object this$year = this.getYear();
        final Object other$year = other.getYear();
        if (this$year == null ? other$year != null : !this$year.equals(other$year)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogOperateMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $blogId = this.getBlogId();
        result = result * PRIME + ($blogId == null ? 43 : $blogId.hashCode());
        final Object $typeEnum = this.getTypeEnum();
        result = result * PRIME + ($typeEnum == null ? 43 : $typeEnum.hashCode());
        final Object $year = this.getYear();
        result = result * PRIME + ($year == null ? 43 : $year.hashCode());
        return result;
    }

    public String toString() {
        return "BlogOperateMessage(blogId=" + this.getBlogId() + ", typeEnum=" + this.getTypeEnum() + ", year=" + this.getYear() + ")";
    }
}
