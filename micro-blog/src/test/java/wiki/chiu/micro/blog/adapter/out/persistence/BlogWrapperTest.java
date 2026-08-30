package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.outbox.OutboxService;

class BlogWrapperTest {

  private final BlogRepository blogs = Mockito.mock(BlogRepository.class);
  private final BlogSensitiveContentRepository sensitiveContents =
      Mockito.mock(BlogSensitiveContentRepository.class);
  private final OutboxService outbox = Mockito.mock(OutboxService.class);
  private final BlogWrapper wrapper = new BlogWrapper(blogs, sensitiveContents, outbox);

  @Test
  void updateConflictStopsAssociationWritesAndOutbox() {
    BlogEntity candidate = blog(7L, 2L);
    when(blogs.updateByIdAndEventRevision(
            7L,
            1L,
            2L,
            candidate.getTitle(),
            candidate.getDescription(),
            candidate.getContent(),
            candidate.getStatus(),
            candidate.getLink(),
            candidate.getUpdated()))
        .thenReturn(0);

    assertThrows(
        BaseException.class,
        () ->
            wrapper.saveOrUpdate(
                candidate,
                1L,
                List.of(11L),
                List.of(),
                new BlogEventContext(BlogOperateEnum.UPDATE, 42L, 10L, 3L)));

    verify(sensitiveContents, never()).deleteAllByIdInBatch(List.of(11L));
    verifyNoInteractions(outbox);
  }

  @Test
  void batchDeleteConflictDoesNotEnqueueAnyEvent() {
    BlogEntity first = blog(7L, 2L);
    BlogEntity second = blog(8L, 4L);
    when(blogs.deleteByIdAndEventRevision(7L, 1L)).thenReturn(1);
    when(blogs.deleteByIdAndEventRevision(8L, 3L)).thenReturn(0);

    assertThrows(
        BaseException.class,
        () ->
            wrapper.deleteByIds(
                List.of(first, second),
                List.of(11L, 12L),
                new BlogEventContext(BlogOperateEnum.REMOVE, 42L, 8L, null)));

    verify(blogs).deleteByIdAndEventRevision(7L, 1L);
    verify(blogs).deleteByIdAndEventRevision(8L, 3L);
    verify(sensitiveContents, never()).deleteAllByIdInBatch(List.of(11L, 12L));
    verifyNoInteractions(outbox);
  }

  private BlogEntity blog(Long id, Long eventRevision) {
    return BlogEntity.builder()
        .id(id)
        .userId(42L)
        .title("title")
        .description("description")
        .content("content")
        .created(LocalDateTime.of(2026, 8, 15, 10, 0))
        .updated(LocalDateTime.of(2026, 8, 15, 11, 0))
        .status(0)
        .link("link")
        .readCount(0L)
        .eventRevision(eventRevision)
        .build();
  }
}
