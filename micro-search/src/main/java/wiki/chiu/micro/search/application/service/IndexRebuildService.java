package wiki.chiu.micro.search.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.model.IndexTransition;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.model.SearchIndexRebuildResult;
import wiki.chiu.micro.search.application.port.in.RebuildSearchIndex;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;

public final class IndexRebuildService implements RebuildSearchIndex {

    private final BlogIndexSource source;
    private final BlogIndexMaintenance indexes;
    private final SearchRebuildControl control;
    private final int batchSize;

    public IndexRebuildService(
        BlogIndexSource source,
        BlogIndexMaintenance indexes,
        SearchRebuildControl control,
        int batchSize) {
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException("rebuild batch size must be between 1 and 1000");
        }
        this.source = source;
        this.indexes = indexes;
        this.control = control;
        this.batchSize = batchSize;
    }

    @Override
    public SearchIndexRebuildResult rebuild() {
        return control.runExclusive(this::rebuildUnderLock);
    }

    private SearchIndexRebuildResult rebuildUnderLock() {
        control.requireQuiescent();
        IndexSourceStatus before = requireSourceReady();
        Map<SearchIndexKind, String> previous = indexes.currentIndexes();

        List<IndexTransition> targets = new ArrayList<>(3);
        targets.add(
            rebuildPublic(previous.get(SearchIndexKind.PUBLIC_BLOGS), before.total()));
        targets.add(rebuildAdmin(previous.get(SearchIndexKind.ADMIN_BLOGS)));
        targets.add(rebuildRevisions(previous.get(SearchIndexKind.REVISIONS)));

        IndexSourceStatus after = requireSourceReady();
        if (before.total() != after.total()) {
            throw new IndexRebuildRejectedException(
                "blog source changed during search index rebuild");
        }
        control.requireQuiescent();
        indexes.activate(targets);
        return new SearchIndexRebuildResult(targets);
    }

    private IndexTransition rebuildPublic(String previous, long expectedDocuments) {
        return rebuild(
            SearchIndexKind.PUBLIC_BLOGS,
            previous,
            expectedDocuments,
            offset -> source.snapshots(offset, batchSize),
            change -> change.blog().id(),
            change -> change.revision(),
            indexes::writePublicSnapshots);
    }

    private IndexTransition rebuildAdmin(String previous) {
        return rebuild(
            SearchIndexKind.ADMIN_BLOGS,
            previous,
            null,
            offset -> source.adminSnapshots(offset, batchSize),
            change -> change.blog().id(),
            null,
            indexes::writeAdminSnapshots);
    }

    private IndexTransition rebuildRevisions(String previous) {
        return rebuild(
            SearchIndexKind.REVISIONS,
            previous,
            null,
            offset -> source.revisionSnapshots(offset, batchSize),
            BlogRevisionIndexEntry::id,
            BlogRevisionIndexEntry::id,
            indexes::writeRevisionSnapshots);
    }

    private <T> IndexTransition rebuild(
        SearchIndexKind kind,
        String previous,
        Long expectedDocuments,
        Function<Long, List<T>> fetch,
        Function<T, Long> id,
        Function<T, Long> revision,
        WriteBatch<T> writer) {
        String target = indexes.createIndex(kind);
        long offset = 0;
        long copied = 0;
        while (true) {
            List<T> batch = fetch.apply(offset);
            if (batch.isEmpty()) {
                break;
            }
            if (batch.size() > batchSize) {
                throw new IndexRebuildRejectedException(
                    "source returned an oversized snapshot batch for " + kind);
            }
            for (T item : batch) {
                long idValue = id.apply(item);
                if (idValue <= offset
                    || (revision != null && revision.apply(item) < 1)) {
                    throw new IndexRebuildRejectedException(
                        "source returned invalid ordering or revision for " + kind);
                }
                offset = idValue;
            }
            writer.write(target, batch);
            copied += batch.size();
        }
        if (expectedDocuments != null && copied != expectedDocuments) {
            throw new IndexRebuildRejectedException(
                "source and rebuilt document counts do not agree for " + kind);
        }
        if (indexes.refreshAndCount(kind, target) != copied) {
            throw new IndexRebuildRejectedException(
                "source and rebuilt index counts do not agree for " + kind);
        }
        return new IndexTransition(kind, previous, target, copied);
    }

    private IndexSourceStatus requireSourceReady() {
        IndexSourceStatus status = source.status();
        if (!status.ready()) {
            throw new IndexRebuildRejectedException(
                "blog source must be read-only with an empty BLOG outbox");
        }
        return status;
    }

    @FunctionalInterface
    private interface WriteBatch<T> {
        void write(String index, List<T> batch);
    }
}
