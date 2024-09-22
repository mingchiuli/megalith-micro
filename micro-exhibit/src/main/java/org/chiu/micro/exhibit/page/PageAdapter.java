package org.chiu.micro.exhibit.page;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-29 12:58 am
 */
public class PageAdapter<T> {

    private List<T> content;

    private long totalElements;

    private int pageNumber;

    private int pageSize;

    private boolean first;

    private boolean last;

    private boolean empty;

    private int totalPages;

    public PageAdapter(List<T> content, long totalElements, int pageNumber, int pageSize, boolean first, boolean last, boolean empty, int totalPages) {
        this.content = content;
        this.totalElements = totalElements;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.first = first;
        this.last = last;
        this.empty = empty;
        this.totalPages = totalPages;
    }

    public PageAdapter() {
    }

    public static <T> PageAdapterBuilder<T> builder() {
        return new PageAdapterBuilder<T>();
    }

    public List<T> getContent() {
        return this.content;
    }

    public long getTotalElements() {
        return this.totalElements;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public boolean isFirst() {
        return this.first;
    }

    public boolean isLast() {
        return this.last;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PageAdapter)) return false;
        final PageAdapter<?> other = (PageAdapter<?>) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        if (this.getTotalElements() != other.getTotalElements()) return false;
        if (this.getPageNumber() != other.getPageNumber()) return false;
        if (this.getPageSize() != other.getPageSize()) return false;
        if (this.isFirst() != other.isFirst()) return false;
        if (this.isLast() != other.isLast()) return false;
        if (this.isEmpty() != other.isEmpty()) return false;
        if (this.getTotalPages() != other.getTotalPages()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PageAdapter;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final long $totalElements = this.getTotalElements();
        result = result * PRIME + (int) ($totalElements >>> 32 ^ $totalElements);
        result = result * PRIME + this.getPageNumber();
        result = result * PRIME + this.getPageSize();
        result = result * PRIME + (this.isFirst() ? 79 : 97);
        result = result * PRIME + (this.isLast() ? 79 : 97);
        result = result * PRIME + (this.isEmpty() ? 79 : 97);
        result = result * PRIME + this.getTotalPages();
        return result;
    }

    public String toString() {
        return "PageAdapter(content=" + this.getContent() + ", totalElements=" + this.getTotalElements() + ", pageNumber=" + this.getPageNumber() + ", pageSize=" + this.getPageSize() + ", first=" + this.isFirst() + ", last=" + this.isLast() + ", empty=" + this.isEmpty() + ", totalPages=" + this.getTotalPages() + ")";
    }

    public static class PageAdapterBuilder<T> {
        private List<T> content;
        private long totalElements;
        private int pageNumber;
        private int pageSize;
        private boolean first;
        private boolean last;
        private boolean empty;
        private int totalPages;

        PageAdapterBuilder() {
        }

        public PageAdapterBuilder<T> content(List<T> content) {
            this.content = content;
            return this;
        }

        public PageAdapterBuilder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public PageAdapterBuilder<T> pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PageAdapterBuilder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageAdapterBuilder<T> first(boolean first) {
            this.first = first;
            return this;
        }

        public PageAdapterBuilder<T> last(boolean last) {
            this.last = last;
            return this;
        }

        public PageAdapterBuilder<T> empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public PageAdapterBuilder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PageAdapter<T> build() {
            return new PageAdapter<T>(this.content, this.totalElements, this.pageNumber, this.pageSize, this.first, this.last, this.empty, this.totalPages);
        }

        public String toString() {
            return "PageAdapter.PageAdapterBuilder(content=" + this.content + ", totalElements=" + this.totalElements + ", pageNumber=" + this.pageNumber + ", pageSize=" + this.pageSize + ", first=" + this.first + ", last=" + this.last + ", empty=" + this.empty + ", totalPages=" + this.totalPages + ")";
        }
    }
}
