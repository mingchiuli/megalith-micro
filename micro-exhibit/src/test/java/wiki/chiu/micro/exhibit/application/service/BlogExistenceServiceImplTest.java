package wiki.chiu.micro.exhibit.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;

class BlogExistenceServiceImplTest {

    private final BlogExistenceStore store = mock(BlogExistenceStore.class);
    private final BlogCatalog catalog = mock(BlogCatalog.class);
    private final BlogExistenceServiceImpl service = service(2);

    @Test
    void onlyReadyAbsentStateRejectsRequest() {
        when(store.lookup(1L)).thenReturn(BlogExistenceStore.State.PRESENT);
        when(store.lookup(2L)).thenReturn(BlogExistenceStore.State.ABSENT);
        when(store.lookup(3L)).thenReturn(BlogExistenceStore.State.UNKNOWN);

        assertDoesNotThrow(() -> service.check(1L));
        assertThrows(MissException.class, () -> service.check(2L));
        assertDoesNotThrow(() -> service.check(3L));
    }

    @Test
    void delegatesCreateRecoveryAndDeleteMarks() {
        service.markPresent(7L);
        service.markAbsent(7L);

        InOrder order = inOrder(store);
        order.verify(store).markPresent(7L);
        order.verify(store).markAbsent(7L);
    }

    @Test
    void rebuildsAllKeysetPagesBeforePublishing() {
        BlogExistenceStore.Rebuild rebuild = mock(BlogExistenceStore.Rebuild.class);
        when(store.tryBeginRebuild()).thenReturn(Optional.of(rebuild));
        when(catalog.findIdsAfter(0L, 2)).thenReturn(List.of(1L, 2L));
        when(catalog.findIdsAfter(2L, 2)).thenReturn(List.of(5L));

        service.rebuildIfRequired();

        InOrder order = inOrder(catalog, rebuild);
        order.verify(catalog).findIdsAfter(0L, 2);
        order.verify(rebuild).addAll(List.of(1L, 2L));
        order.verify(catalog).findIdsAfter(2L, 2);
        order.verify(rebuild).addAll(List.of(5L));
        order.verify(rebuild).publish();
        order.verify(rebuild).close();
    }

    @Test
    void failedPageLoadClosesStagingWithoutPublishing() {
        BlogExistenceStore.Rebuild rebuild = mock(BlogExistenceStore.Rebuild.class);
        when(store.tryBeginRebuild()).thenReturn(Optional.of(rebuild));
        when(catalog.findIdsAfter(0L, 2)).thenThrow(new IllegalStateException("downstream"));

        assertDoesNotThrow(service::rebuildIfRequired);

        verify(rebuild, never()).publish();
        verify(rebuild).close();
    }

    @Test
    void skipsScanWhenIndexIsAlreadyReadyOrAnotherReplicaOwnsLock() {
        when(store.tryBeginRebuild()).thenReturn(Optional.empty());

        service.rebuildIfRequired();

        verify(catalog, never()).findIdsAfter(0L, 2);
    }

    @Test
    void rejectsBatchSizeOutsideInternalApiLimit() {
        assertThrows(IllegalArgumentException.class, () -> service(1001));
    }

    private BlogExistenceServiceImpl service(int batchSize) {
        return new BlogExistenceServiceImpl(
            store, catalog, batchSize, new SimpleMeterRegistry());
    }
}
