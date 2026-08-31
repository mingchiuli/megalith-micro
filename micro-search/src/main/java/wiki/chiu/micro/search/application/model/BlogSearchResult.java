package wiki.chiu.micro.search.application.model;

import java.util.List;

public record BlogSearchResult(long total, int page, int pageSize, List<Long> ids) {

    public BlogSearchResult {
        ids = List.copyOf(ids);
    }
}
