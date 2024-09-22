package org.chiu.micro.exhibit.vo;

/**
 * @author mingchiuli
 * @create 2023-03-30 3:15 am
 */
public class BlogHotReadVo {

    private Long id;

    private String title;

    private Long readCount;

    BlogHotReadVo(Long id, String title, Long readCount) {
        this.id = id;
        this.title = title;
        this.readCount = readCount;
    }

    public static BlogHotReadVoBuilder builder() {
        return new BlogHotReadVoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public Long getReadCount() {
        return this.readCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogHotReadVo)) return false;
        final BlogHotReadVo other = (BlogHotReadVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$readCount = this.getReadCount();
        final Object other$readCount = other.getReadCount();
        if (this$readCount == null ? other$readCount != null : !this$readCount.equals(other$readCount)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogHotReadVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $readCount = this.getReadCount();
        result = result * PRIME + ($readCount == null ? 43 : $readCount.hashCode());
        return result;
    }

    public String toString() {
        return "BlogHotReadVo(id=" + this.getId() + ", title=" + this.getTitle() + ", readCount=" + this.getReadCount() + ")";
    }

    public static class BlogHotReadVoBuilder {
        private Long id;
        private String title;
        private Long readCount;

        BlogHotReadVoBuilder() {
        }

        public BlogHotReadVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogHotReadVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogHotReadVoBuilder readCount(Long readCount) {
            this.readCount = readCount;
            return this;
        }

        public BlogHotReadVo build() {
            return new BlogHotReadVo(this.id, this.title, this.readCount);
        }

        public String toString() {
            return "BlogHotReadVo.BlogHotReadVoBuilder(id=" + this.id + ", title=" + this.title + ", readCount=" + this.readCount + ")";
        }
    }
}
