package wiki.chiu.micro.blog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogStatisticsGateway;

class BlogStatisticsSyncServiceTest {

    @Test
    void aFailedCycleRestartsFromTheBeginningWithAbsoluteCounts() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogStatisticsGateway gateway = mock(BlogStatisticsGateway.class);
        var first = List.of(new BlogReadCount(2, 17), new BlogReadCount(9, 120));
        var second = List.of(new BlogReadCount(20, 31));
        when(blogs.findReadCountsAfter(0, 2)).thenReturn(first);
        when(blogs.findReadCountsAfter(9, 2)).thenReturn(second);
        doThrow(new IllegalStateException("timeout")).doNothing().when(gateway).updateReadCounts(second);
        var service = new BlogStatisticsSyncService(blogs, gateway);

        assertThatThrownBy(() -> service.synchronize(2)).hasMessage("timeout");
        assertThat(service.synchronize(2)).isEqualTo(3);

        verify(gateway, times(2)).updateReadCounts(first);
        verify(gateway, times(2)).updateReadCounts(second);
    }
}
