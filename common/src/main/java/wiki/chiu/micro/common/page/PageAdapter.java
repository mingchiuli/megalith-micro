package wiki.chiu.micro.common.page;

import java.util.Collections;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-29 12:58 am
 */
public record PageAdapter<T> (
        List<T> content,

        long totalElements,

        int pageNumber,

        int pageSize,

        boolean first,

        boolean last,

        boolean empty,

        int totalPages) {

    public static  <T> PageAdapterBuilder<T> builder() {
        return new PageAdapterBuilder<>();
    }

    public static <T> PageAdapter<T> emptyPage() {
        return new PageAdapter<>(Collections.emptyList(), 0, 1, 1, true, true, true, 0);
    }

    public PageAdapter(List<T> content, PageAdapter<T> pageAdapter) {
        this(content, pageAdapter.totalElements(), pageAdapter.pageNumber(), pageAdapter.pageSize(), pageAdapter.first(), pageAdapter.last(), pageAdapter.empty(), pageAdapter.totalPages());
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
            return new PageAdapter<>(this.content, this.totalElements, this.pageNumber, this.pageSize, this.first, this.last, this.empty, this.totalPages);
        }

    }
}
