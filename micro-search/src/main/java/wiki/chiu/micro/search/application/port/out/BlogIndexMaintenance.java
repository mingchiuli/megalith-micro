package wiki.chiu.micro.search.application.port.out;

import java.util.Map;
import java.util.List;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexTransition;
import wiki.chiu.micro.search.application.model.SearchIndexKind;

public interface BlogIndexMaintenance {

    Map<SearchIndexKind, String> currentIndexes();

    String createIndex(SearchIndexKind kind);

    void writePublicSnapshots(String index, List<BlogIndexChange> snapshots);

    void writeAdminSnapshots(String index, List<BlogAdminIndexChange> snapshots);

    void writeRevisionSnapshots(String index, List<BlogRevisionIndexEntry> revisions);

    long refreshAndCount(SearchIndexKind kind, String index);

    void activate(List<IndexTransition> transitions);
}
