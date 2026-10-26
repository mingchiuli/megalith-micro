package wiki.chiu.micro.blog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.adapter.out.persistence.BlogWrapper;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;

class BlogQueryServiceImplTest {

    @Test
    void returnsIdsAfterExclusiveCursor() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogQueryServiceImpl service = new BlogQueryServiceImpl(blogs, mock(BlogWrapper.class));
        when(blogs.findIdsAfter(7L, 1000)).thenReturn(List.of(8L, 11L));

        assertEquals(List.of(8L, 11L), service.findIdsAfter(7L, 1000));
        verify(blogs).findIdsAfter(7L, 1000);
    }

}
