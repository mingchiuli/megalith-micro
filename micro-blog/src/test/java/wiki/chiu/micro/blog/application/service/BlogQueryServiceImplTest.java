package wiki.chiu.micro.blog.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.adapter.out.persistence.BlogWrapper;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;

class BlogQueryServiceImplTest {

    @Test
    void returnsIdsAfterExclusiveCursor() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogQueryServiceImpl service = new BlogQueryServiceImpl(blogs, mock(BlogWrapper.class));
        when(blogs.findIdsAfter(7L, 1000)).thenReturn(List.of(8L, 11L));

        assertEquals(List.of(8L, 11L), service.findIdsAfter(7L, 1000));
        verify(blogs).findIdsAfter(7L, 1000);
    }

    @Test
    void firstPageMayBeEmptyButLaterEmptyPagesAreMissing() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogQueryServiceImpl service = new BlogQueryServiceImpl(blogs, mock(BlogWrapper.class));
        List<Integer> statuses = List.of(0, 2, 1);
        when(blogs.findPage(eq(1), eq(10), eq(statuses)))
            .thenReturn(
                PageAdapter.emptyPage());
        when(blogs.findPage(eq(2), eq(10), eq(statuses)))
            .thenReturn(
                PageAdapter.<wiki.chiu.micro.blog.domain.BlogEntity>builder()
                    .content(List.of())
                    .pageNumber(2)
                    .pageSize(10)
                    .empty(true)
                    .build());

        assertDoesNotThrow(() -> service.findPage(1, 10));
        assertThrows(MissException.class, () -> service.findPage(2, 10));
    }
}
