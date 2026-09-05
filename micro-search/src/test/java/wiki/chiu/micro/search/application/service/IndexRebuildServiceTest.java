package wiki.chiu.micro.search.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
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

        assertThat(result.documents()).isEqualTo(3);
        var order = org.mockito.Mockito.inOrder(indexes);
        order.verify(indexes).currentIndex();
        order.verify(indexes).createIndex();
        order.verify(indexes).writeSnapshots("new", List.of(snapshot(7), snapshot(9)));
        order.verify(indexes).writeSnapshots("new", List.of(snapshot(15)));
        order.verify(indexes).refreshAndCount("new");
        order.verify(indexes).activate("old", "new");
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
            .writeSnapshots("new", List.of(snapshot(15)));

        assertThatThrownBy(service::rebuild).hasMessage("bulk rejected");
        verify(indexes, never()).activate(anyString(), anyString());
    }

    @Test
    void changedSourceOrMissingDocumentsPreventsActivation() {
        prepare();
        when(indexes.refreshAndCount("new")).thenReturn(2L);

        assertThatThrownBy(service::rebuild).hasMessageContaining("counts do not agree");
        verify(indexes, never()).activate(anyString(), anyString());
    }

    private void prepare() {
        when(source.status()).thenReturn(new IndexSourceStatus(true, 0, 0, 3));
        when(source.snapshots(0, 2)).thenReturn(List.of(snapshot(7), snapshot(9)));
        when(source.snapshots(9, 2)).thenReturn(List.of(snapshot(15)));
        when(indexes.currentIndex()).thenReturn("old");
        when(indexes.createIndex()).thenReturn("new");
        when(indexes.refreshAndCount("new")).thenReturn(3L);
    }

    private BlogIndexChange snapshot(long id) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogIndexChange(BlogIndexChange.Operation.CREATE, 11,
            new BlogIndexEntry(id, 42L, 0, 120L, "title", "description", "content", date, date));
    }
}
