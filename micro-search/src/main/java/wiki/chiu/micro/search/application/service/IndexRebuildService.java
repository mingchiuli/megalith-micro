package wiki.chiu.micro.search.application.service;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.model.IndexRebuildResult;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
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
        BlogIndexSource source, BlogIndexMaintenance indexes, SearchRebuildControl control, int batchSize) {
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException("rebuild batch size must be between 1 and 1000");
        }
        this.source = source;
        this.indexes = indexes;
        this.control = control;
        this.batchSize = batchSize;
    }

    @Override
    public IndexRebuildResult rebuild() {
        return control.runExclusive(this::rebuildUnderLock);
    }

    private IndexRebuildResult rebuildUnderLock() {
        control.requireQuiescent();
        IndexSourceStatus before = requireSourceReady();
        String previous = indexes.currentIndex();
        String target = indexes.createIndex();
        long afterId = 0;
        long copied = 0;
        while (true) {
            List<BlogIndexChange> batch = source.snapshots(afterId, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            if (batch.size() > batchSize) {
                throw new IndexRebuildRejectedException("source returned an oversized snapshot batch");
            }
            for (BlogIndexChange snapshot : batch) {
                if (snapshot.blog().id() <= afterId || snapshot.revision() < 1
                    || snapshot.operation() == BlogIndexChange.Operation.REMOVE) {
                    throw new IndexRebuildRejectedException("source returned invalid snapshot ordering or revision");
                }
                afterId = snapshot.blog().id();
            }
            indexes.writeSnapshots(target, batch);
            copied += batch.size();
        }
        IndexSourceStatus after = requireSourceReady();
        control.requireQuiescent();
        if (copied != before.total() || copied != after.total() || indexes.refreshAndCount(target) != copied) {
            throw new IndexRebuildRejectedException("source and rebuilt index document counts do not agree");
        }
        indexes.activate(previous, target);
        return new IndexRebuildResult(previous, target, copied);
    }

    private IndexSourceStatus requireSourceReady() {
        IndexSourceStatus status = source.status();
        if (!status.ready()) {
            throw new IndexRebuildRejectedException("blog source must be read-only with an empty BLOG outbox");
        }
        return status;
    }
}
