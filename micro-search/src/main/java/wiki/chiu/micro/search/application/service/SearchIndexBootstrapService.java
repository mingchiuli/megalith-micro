package wiki.chiu.micro.search.application.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.port.in.BootstrapSearchIndexes;
import wiki.chiu.micro.search.application.port.out.BlogBootstrapIndexSource;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.SearchIndexSchema;

public final class SearchIndexBootstrapService implements BootstrapSearchIndexes {

    private final SearchIndexSchema schema;
    private final BlogBootstrapIndexSource source;
    private final BlogIndexWriter writer;

    public SearchIndexBootstrapService(
        SearchIndexSchema schema, BlogBootstrapIndexSource source, BlogIndexWriter writer) {
        this.schema = schema;
        this.source = source;
        this.writer = writer;
    }

    @Override
    public void bootstrap(int batchSize) {
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException("bootstrap batch size must be between 1 and 1000");
        }
        schema.ensureAliases();
        bootstrapPublic(batchSize);
        bootstrapAdmin(batchSize);
        bootstrapRevisions(batchSize);
    }

    private void bootstrapPublic(int batchSize) {
        long afterId = 0;
        while (true) {
            List<BlogIndexChange> snapshots = source.publicSnapshots(afterId, batchSize);
            validateBatch(snapshots, afterId, batchSize, snapshot -> snapshot.blog().id());
            if (snapshots.isEmpty()) {
                return;
            }
            writer.writeAll(snapshots);
            afterId = snapshots.getLast().blog().id();
        }
    }

    private void bootstrapAdmin(int batchSize) {
        long afterId = 0;
        while (true) {
            List<BlogAdminIndexChange> snapshots = source.adminSnapshots(afterId, batchSize);
            validateBatch(snapshots, afterId, batchSize, snapshot -> snapshot.blog().id());
            if (snapshots.isEmpty()) {
                return;
            }
            writer.writeAdminAll(snapshots);
            afterId = snapshots.getLast().blog().id();
        }
    }

    private void bootstrapRevisions(int batchSize) {
        long afterRevisionId = 0;
        while (true) {
            List<BlogAdminIndexChange> revisions =
                source.revisionSnapshots(afterRevisionId, batchSize);
            validateBatch(
                revisions,
                afterRevisionId,
                batchSize,
                revision -> revision.revision().id());
            if (revisions.isEmpty()) {
                return;
            }
            writer.writeRevisionAll(revisions);
            afterRevisionId = revisions.getLast().revision().id();
        }
    }

    private static <T> void validateBatch(
        List<T> values, long afterId, int batchSize, Function<T, Long> id) {
        if (values.size() > batchSize) {
            throw new IllegalStateException("index bootstrap source returned an oversized batch");
        }
        if (!values.isEmpty() && id.apply(values.getFirst()) <= afterId) {
            throw new IllegalStateException("index bootstrap source did not advance its cursor");
        }
        boolean ordered =
            IntStream.range(1, values.size())
                .allMatch(index -> id.apply(values.get(index - 1)) < id.apply(values.get(index)));
        if (!ordered) {
            throw new IllegalStateException("index bootstrap source returned unordered ids");
        }
    }
}
