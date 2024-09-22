package org.chiu.micro.search.vo;

import java.util.List;

public class BlogSearchVo {

    private Long total;

    private Integer currentPage;

    private Integer size;

    private List<Long> ids;

    BlogSearchVo(Long total, Integer currentPage, Integer size, List<Long> ids) {
        this.total = total;
        this.currentPage = currentPage;
        this.size = size;
        this.ids = ids;
    }

    public static BlogSearchVoBuilder builder() {
        return new BlogSearchVoBuilder();
    }

    public Long getTotal() {
        return this.total;
    }

    public Integer getCurrentPage() {
        return this.currentPage;
    }

    public Integer getSize() {
        return this.size;
    }

    public List<Long> getIds() {
        return this.ids;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogSearchVo)) return false;
        final BlogSearchVo other = (BlogSearchVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$total = this.getTotal();
        final Object other$total = other.getTotal();
        if (this$total == null ? other$total != null : !this$total.equals(other$total)) return false;
        final Object this$currentPage = this.getCurrentPage();
        final Object other$currentPage = other.getCurrentPage();
        if (this$currentPage == null ? other$currentPage != null : !this$currentPage.equals(other$currentPage))
            return false;
        final Object this$size = this.getSize();
        final Object other$size = other.getSize();
        if (this$size == null ? other$size != null : !this$size.equals(other$size)) return false;
        final Object this$ids = this.getIds();
        final Object other$ids = other.getIds();
        if (this$ids == null ? other$ids != null : !this$ids.equals(other$ids)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogSearchVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $total = this.getTotal();
        result = result * PRIME + ($total == null ? 43 : $total.hashCode());
        final Object $currentPage = this.getCurrentPage();
        result = result * PRIME + ($currentPage == null ? 43 : $currentPage.hashCode());
        final Object $size = this.getSize();
        result = result * PRIME + ($size == null ? 43 : $size.hashCode());
        final Object $ids = this.getIds();
        result = result * PRIME + ($ids == null ? 43 : $ids.hashCode());
        return result;
    }

    public String toString() {
        return "BlogSearchVo(total=" + this.getTotal() + ", currentPage=" + this.getCurrentPage() + ", size=" + this.getSize() + ", ids=" + this.getIds() + ")";
    }

    public static class BlogSearchVoBuilder {
        private Long total;
        private Integer currentPage;
        private Integer size;
        private List<Long> ids;

        BlogSearchVoBuilder() {
        }

        public BlogSearchVoBuilder total(Long total) {
            this.total = total;
            return this;
        }

        public BlogSearchVoBuilder currentPage(Integer currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public BlogSearchVoBuilder size(Integer size) {
            this.size = size;
            return this;
        }

        public BlogSearchVoBuilder ids(List<Long> ids) {
            this.ids = ids;
            return this;
        }

        public BlogSearchVo build() {
            return new BlogSearchVo(this.total, this.currentPage, this.size, this.ids);
        }

        public String toString() {
            return "BlogSearchVo.BlogSearchVoBuilder(total=" + this.total + ", currentPage=" + this.currentPage + ", size=" + this.size + ", ids=" + this.ids + ")";
        }
    }
}
