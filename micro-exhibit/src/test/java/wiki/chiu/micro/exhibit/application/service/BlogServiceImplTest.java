package wiki.chiu.micro.exhibit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.exhibit.adapter.out.composite.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.adapter.out.composite.BlogWrapper;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;

class BlogServiceImplTest {

    private final BlogWrapper blogWrapper = mock(BlogWrapper.class);
    private final ExhibitMetrics metrics = mock(ExhibitMetrics.class);
    private BlogServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
            new BlogServiceImpl(
                mock(BlogSensitiveWrapper.class),
                mock(BlogCatalog.class),
                blogWrapper,
                metrics);
    }

    @Test
    void consumesTokenAtomicallyBeforeReturningBlog() {
        when(metrics.consumeReadToken(7L, "token")).thenReturn(true);
        when(blogWrapper.findById(7L))
            .thenReturn(
                BlogExhibitDto.builder()
                    .title("Protected")
                    .description("description")
                    .content("content")
                    .nickname("author")
                    .avatar("")
                    .readCount(1L)
                    .build());

        var blog = service.getLockedBlog(7L, " token ");

        assertEquals("Protected", blog.title());
        verify(blogWrapper).incrementViews(7L);
        verify(metrics).consumeReadToken(7L, "token");
    }

    @Test
    void rejectsTokenThatWasNotConsumed() {
        when(metrics.consumeReadToken(7L, "wrong")).thenReturn(false);

        assertThrows(MissException.class, () -> service.getLockedBlog(7L, "wrong"));
        verify(blogWrapper, never()).findById(7L);
    }
}
