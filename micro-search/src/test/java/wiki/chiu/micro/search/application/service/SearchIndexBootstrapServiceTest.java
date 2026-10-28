package wiki.chiu.micro.search.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.port.out.BlogBootstrapIndexSource;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.SearchIndexSchema;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

class SearchIndexBootstrapServiceTest {

    private final SearchIndexSchema schema = mock(SearchIndexSchema.class);
    private final BlogBootstrapIndexSource source = mock(BlogBootstrapIndexSource.class);
    private final BlogIndexWriter writer = mock(BlogIndexWriter.class);
    private final SearchIndexBootstrapService service =
        new SearchIndexBootstrapService(schema, source, writer);

    @Test
    void createsAliasesAndBackfillsAllThreeIndexesInOrder() {
        BlogIndexChange publicFirst = publicChange(1);
        BlogIndexChange publicSecond = publicChange(3);
        BlogAdminIndexChange adminFirst = adminChange(2);
        BlogAdminIndexChange revisionFirst = revisionChange(4);
        when(source.publicSnapshots(0, 2)).thenReturn(List.of(publicFirst, publicSecond));
        when(source.publicSnapshots(3, 2)).thenReturn(List.of());
        when(source.adminSnapshots(0, 2)).thenReturn(List.of(adminFirst));
        when(source.adminSnapshots(2, 2)).thenReturn(List.of());
        when(source.revisionSnapshots(0, 2)).thenReturn(List.of(revisionFirst));
        when(source.revisionSnapshots(4, 2)).thenReturn(List.of());

        service.bootstrap(2);

        var order = inOrder(schema, writer);
        order.verify(schema).ensureAliases();
        order.verify(writer).writeAll(List.of(publicFirst, publicSecond));
        order.verify(writer).writeAdminAll(List.of(adminFirst));
        order.verify(writer).writeRevisionAll(List.of(revisionFirst));
        verify(source, never()).publicSnapshots(2, 2);
    }

    @Test
    void rejectsASourceThatDoesNotAdvanceItsCursor() {
        when(source.publicSnapshots(0, 2)).thenReturn(List.of(publicChange(1)));
        when(source.publicSnapshots(1, 2)).thenReturn(List.of(publicChange(1)));

        assertThatThrownBy(() -> service.bootstrap(2))
            .hasMessageContaining("did not advance");
        verify(writer).writeAll(List.of(publicChange(1)));
        verify(writer, never()).writeAdminAll(anyList());
        verify(writer, never()).writeRevisionAll(anyList());
    }

    @Test
    void rejectsInvalidBatchSizeBeforeCreatingAliases() {
        assertThatThrownBy(() -> service.bootstrap(0))
            .hasMessageContaining("between 1 and 1000");
        verifyNoInteractions(schema, source, writer);
    }

    private static BlogIndexChange publicChange(long id) {
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        return new BlogIndexChange(
            BlogIndexChange.Operation.REPAIR,
            id,
            new BlogIndexEntry(
                id,
                42L,
                0,
                10L,
                "title",
                "description",
                "content",
                "https://cdn.example/" + id + ".png",
                date,
                date,
                1L,
                null,
                date,
                null,
                0,
                BlogRenderMode.NORMAL,
                List.of()));
    }

    private static BlogAdminIndexChange adminChange(long id) {
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        return new BlogAdminIndexChange(
            "BOOTSTRAP",
            new BlogAdminSnapshot(
                id, 42L, 1L, date, null, 0, 1L, 1L, 10L, date),
            new BlogDraftSnapshot(
                id,
                1L,
                1L,
                "draft",
                "description",
                "content",
                "https://cdn.example/" + id + ".png",
                BlogRenderMode.NORMAL,
                42L,
                date,
                List.of()),
            revision(id, id));
    }

    private static BlogAdminIndexChange revisionChange(long id) {
        return new BlogAdminIndexChange("BOOTSTRAP", null, null, revision(id, 7L));
    }

    private static BlogRevisionSnapshot revision(long id, long blogId) {
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        return new BlogRevisionSnapshot(
            id,
            blogId,
            1L,
            BlogRevisionOrigin.PUBLICATION,
            null,
            "title",
            "description",
            "content",
            "https://cdn.example/" + id + ".png",
            BlogRenderMode.NORMAL,
            date,
            42L,
            date,
            List.of());
    }
}
