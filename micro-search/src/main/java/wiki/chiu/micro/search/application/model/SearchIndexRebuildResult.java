package wiki.chiu.micro.search.application.model;

import java.util.List;

public record SearchIndexRebuildResult(List<IndexTransition> indexes) {

    public SearchIndexRebuildResult {
        indexes = List.copyOf(indexes);
    }
}
