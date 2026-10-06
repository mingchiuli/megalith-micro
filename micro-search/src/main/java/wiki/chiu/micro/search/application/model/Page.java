package wiki.chiu.micro.search.application.model;

import java.util.List;

/**
 * A page of use-case rows. The HTTP envelope the clients receive is built from it by the adapter
 * that renders the response.
 */
public record Page<T>(
    List<T> content,
    long totalElements,
    int pageNumber,
    int pageSize,
    boolean first,
    boolean last,
    boolean empty,
    int totalPages) {

    public Page {
        content = List.copyOf(content);
    }

    /** The envelope of a query that matched nothing. */
    public static <T> Page<T> emptyPage() {
        return new Page<>(List.of(), 0, 1, 1, true, true, true, 0);
    }

    /** Carries this page's bounds with a different row type. */
    public <R> Page<R> withContent(List<R> content) {
        return new Page<>(
            content, totalElements, pageNumber, pageSize, first, last, empty, totalPages);
    }
}
