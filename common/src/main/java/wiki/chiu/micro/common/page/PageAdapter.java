package wiki.chiu.micro.common.page;

import java.util.Collections;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-29 12:58 am
 */
public record PageAdapter<T>(
        List<T> content,
        long totalElements,
        int pageNumber,
        int pageSize,
        boolean first,
        boolean last,
        boolean empty,
        int totalPages) {

    public static <T> PageAdapterBuilder<T> builder() {
        return new PageAdapterBuilder<>(Collections.emptyList(), 0, 1, 1, true, true, true, 0);
    }

    public static <T> PageAdapter<T> emptyPage() {
        return new PageAdapter<>(Collections.emptyList(), 0, 1, 1, true, true, true, 0);
    }

    public PageAdapter(List<T> content, PageAdapter<T> pageAdapter) {
        this(content, pageAdapter.totalElements(), pageAdapter.pageNumber(), pageAdapter.pageSize(), pageAdapter.first(), pageAdapter.last(), pageAdapter.empty(), pageAdapter.totalPages());
    }

    public record PageAdapterBuilder<T>(
            List<T> content,
            long totalElements,
            int pageNumber,
            int pageSize,
            boolean first,
            boolean last,
            boolean empty,
            int totalPages
    ) {

        public PageAdapterBuilder<T> content(List<T> content) {
            return new PageAdapterBuilder<>(content, this.totalElements, this.pageNumber, this.pageSize, this.first, this.last, this.empty, this.totalPages);
        }

        public PageAdapterBuilder<T> totalElements(long totalElements) {
            return new PageAdapterBuilder<>(this.content, totalElements, this.pageNumber, this.pageSize, this.first, this.last, this.empty, this.totalPages);
        }

        public PageAdapterBuilder<T> pageNumber(int pageNumber) {
            return new PageAdapterBuilder<>(this.content, this.totalElements, pageNumber, this.pageSize, this.first, this.last, this.empty, this.totalPages);
        }

        public PageAdapterBuilder<T> pageSize(int pageSize) {
            return new PageAdapterBuilder<>(this.content, this.totalElements, this.pageNumber, pageSize, this.first, this.last, this.empty, this.totalPages);
        }

        public PageAdapterBuilder<T> first(boolean first) {
            return new PageAdapterBuilder<>(this.content, this.totalElements, this.pageNumber, this.pageSize, first, this.last, this.empty, this.totalPages);
        }

        public PageAdapterBuilder<T> last(boolean last) {
            return new PageAdapterBuilder<>(this.content, this.totalElements, this.pageNumber, this.pageSize, this.first, last, this.empty, this.totalPages);
        }

        public PageAdapterBuilder<T> empty(boolean empty) {
            return new PageAdapterBuilder<>(this.content, this.totalElements, this.pageNumber, this.pageSize, this.first, this.last, empty, this.totalPages);
        }

        public PageAdapterBuilder<T> totalPages(int totalPages) {
            return new PageAdapterBuilder<>(this.content, this.totalElements, this.pageNumber, this.pageSize, this.first, this.last, this.empty, totalPages);
        }

        public PageAdapter<T> build() {
            return new PageAdapter<>(this.content, this.totalElements, this.pageNumber, this.pageSize, this.first, this.last, this.empty, this.totalPages);
        }
    }
}