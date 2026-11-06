package wiki.chiu.micro.search.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

class IndexRebuildServiceTest {

    private final BlogIndexSource source = mock(BlogIndexSource.class);
    private final BlogIndexMaintenance indexes = mock(BlogIndexMaintenance.class);
    private final SearchRebuildControl control = mock(SearchRebuildControl.class);
    private final IndexRebuildService service = new IndexRebuildService(source, indexes, control, 2);

    @BeforeEach
    void configureLock() {
        when(control.runExclusive(any())).thenAnswer(call -> ((Supplier<?>) call.getArgument(0)).get());
    }

    @Test
    void switchesOnlyAfterAllBatchesAreRefreshedAndValidated() {
        prepare();

        var result = service.rebuild();

        assertThat(result.indexes()).extracting(index -> index.documents()).containsExactly(3L, 4L, 0L);
        var order = inOrder(indexes);
        order.verify(indexes).currentIndexes();
        order.verify(indexes).createIndex(SearchIndexKind.PUBLIC_BLOGS);
        order.verify(indexes).writePublicSnapshots("public-new", List.of(snapshot(7), snapshot(9)));
        order.verify(indexes).writePublicSnapshots("public-new", List.of(snapshot(15)));
        order.verify(indexes).refreshAndCount(SearchIndexKind.PUBLIC_BLOGS, "public-new");
        order.verify(indexes).activate(any());
    }

    @Test
    void rejectsUnprocessedQueueMessagesBeforeCreatingAnIndex() {
        doThrow(new IndexRebuildRejectedException("pending retry")).when(control).requireQuiescent();
        assertThatThrownBy(service::rebuild).hasMessage("pending retry");
        verifyNoInteractions(source, indexes);
    }

    @Test
    void rejectsMutableSourceBeforeCreatingAnIndex() {
        when(source.status()).thenReturn(new IndexSourceStatus(false, 0, 0, 3));
        assertThatThrownBy(service::rebuild).hasMessageContaining("read-only");
        verifyNoInteractions(indexes);
    }

    @Test
    void failedBatchKeepsThePreviousAlias() {
        prepare();
        doThrow(new IllegalStateException("bulk rejected")).when(indexes)
            .writePublicSnapshots("public-new", List.of(snapshot(15)));

        assertThatThrownBy(service::rebuild).hasMessage("bulk rejected");
        verify(indexes, never()).activate(any());
    }

    @Test
    void changedSourceOrMissingDocumentsPreventsActivation() {
        prepare();
        when(indexes.refreshAndCount(SearchIndexKind.PUBLIC_BLOGS, "public-new"))
            .thenReturn(2L);

        assertThatThrownBy(service::rebuild).hasMessageContaining("counts do not agree");
        verify(indexes, never()).activate(any());
    }

    private void prepare() {
        when(source.status()).thenReturn(new IndexSourceStatus(true, 0, 0, 4, 3));
        when(source.snapshots(0, 2)).thenReturn(List.of(snapshot(7), snapshot(9)));
        when(source.snapshots(9, 2)).thenReturn(List.of(snapshot(15)));
        when(source.adminSnapshots(0, 2)).thenReturn(List.of(admin(7), admin(9)));
        when(source.adminSnapshots(9, 2)).thenReturn(List.of(admin(15), admin(16)));
        when(source.adminSnapshots(16, 2)).thenReturn(List.of());
        when(source.revisionSnapshots(anyLong(), anyInt())).thenReturn(List.of());
        when(indexes.currentIndexes())
            .thenReturn(
                Map.of(
                    SearchIndexKind.PUBLIC_BLOGS, "public-old",
                    SearchIndexKind.ADMIN_BLOGS, "admin-old",
                    SearchIndexKind.REVISIONS, "revision-old"));
        when(indexes.createIndex(SearchIndexKind.PUBLIC_BLOGS)).thenReturn("public-new");
        when(indexes.createIndex(SearchIndexKind.ADMIN_BLOGS)).thenReturn("admin-new");
        when(indexes.createIndex(SearchIndexKind.REVISIONS)).thenReturn("revision-new");
        when(indexes.refreshAndCount(SearchIndexKind.PUBLIC_BLOGS, "public-new")).thenReturn(3L);
        when(indexes.refreshAndCount(SearchIndexKind.ADMIN_BLOGS, "admin-new")).thenReturn(4L);
        when(indexes.refreshAndCount(SearchIndexKind.REVISIONS, "revision-new")).thenReturn(0L);
    }

    private BlogAdminIndexChange admin(long id) {
        return new BlogAdminIndexChange("DRAFT_UPSERT",
            new BlogAdminSnapshot(id, 42L, null, null, null, 0, 0L, 1L, 0L,
                LocalDateTime.of(2026, 9, 1, 12, 0)), null, null);
    }

    private BlogIndexChange snapshot(long id) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogIndexChange(BlogIndexChange.Operation.CREATE, 11,
            new BlogIndexEntry(id, 42L, 0, 120L, "title", "description", "content", date, date));
    }
}
