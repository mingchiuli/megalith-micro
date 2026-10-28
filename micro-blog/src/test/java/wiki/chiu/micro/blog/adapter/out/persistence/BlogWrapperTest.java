package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogFixtures;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

class BlogWrapperTest {

    private final BlogRepository blogs = Mockito.mock(BlogRepository.class);
    private final BlogRevisionRepository revisions = Mockito.mock(BlogRevisionRepository.class);
    private final BlogDraftRepository drafts = Mockito.mock(BlogDraftRepository.class);
    private final BlogDraftSensitiveContentRepository draftSensitive =
        Mockito.mock(BlogDraftSensitiveContentRepository.class);
    private final BlogRevisionSensitiveContentRepository revisionSensitive =
        Mockito.mock(BlogRevisionSensitiveContentRepository.class);
    private final OutboxEvents outbox = Mockito.mock(OutboxEvents.class);

    @Test
    void maintenanceStopsDestructiveWritersButAllowsViewCounting() {
        BlogWrapper readOnly = wrapper(new BlogMaintenanceMode(true));
        var event = new BlogEventContext(BlogOperateEnum.CREATE, 42L);

        assertThrows(
            BaseException.class,
            () -> readOnly.recoverDeletedBlog(blog(), event));
        assertThrows(
            BaseException.class,
            () -> readOnly.deleteByIds(
                List.of(new BlogDeletionTarget(blog(), identity())), event));
        verifyNoInteractions(blogs, revisions, drafts, draftSensitive, revisionSensitive, outbox);

        readOnly.incrementViews(7L);
        verify(blogs).setReadCount(7L);
    }

    @Test
    void recoveryPreservesRestrictedAccessAndPublishesAnAdminRecoveryEvent() {
        BlogWrapper writer = wrapper(new BlogMaintenanceMode(false));
        BlogSensitiveSpan sensitive = new BlogSensitiveSpan(1, 3, 3);
        Blog hidden =
            new Blog(
                7L,
                42L,
                "title",
                "description",
                "content",
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 11, 0),
                BlogStatusEnum.HIDE.getCode(),
                "link",
                0L,
                3L,
                List.of(sensitive));
        when(blogs.save(any(BlogEntity.class))).thenAnswer(call -> call.getArgument(0));
        when(revisions.save(any(BlogRevisionEntity.class)))
            .thenAnswer(
                call -> {
                    BlogRevisionEntity saved = call.getArgument(0);
                    return new BlogRevisionEntity(
                        11L,
                        saved.getBlogId(),
                        saved.getVersionNo(),
                        saved.getOrigin(),
                        saved.getBaseRevisionId(),
                        saved.getTitle(),
                        saved.getDescription(),
                        saved.getContent(),
                        saved.getLink(),
                        saved.getRenderMode(),
                        saved.getFirstPublishedAt(),
                        saved.getCreatedBy(),
                        saved.getCreatedAt());
                });
        List<Object> messages = captureEvents();

        writer.recoverDeletedBlog(
            hidden, new BlogEventContext(BlogOperateEnum.CREATE, 42L));

        verify(outbox, org.mockito.Mockito.times(2))
            .enqueue(
                eq(OutboxProducer.BLOG),
                eq("BLOG"),
                eq(7L),
                any());
        BlogChangedMessage publicEvent =
            messages.stream()
                .filter(BlogChangedMessage.class::isInstance)
                .map(BlogChangedMessage.class::cast)
                .findFirst()
                .orElseThrow();
        BlogAdminIndexMessage adminEvent =
            messages.stream()
                .filter(BlogAdminIndexMessage.class::isInstance)
                .map(BlogAdminIndexMessage.class::cast)
                .findFirst()
                .orElseThrow();

        assertThat(publicEvent.blogSnapshot().accessMode()).isEqualTo(1);
        assertThat(publicEvent.blogSnapshot().sensitiveContent()).containsExactly(sensitive);
        assertThat(adminEvent.operation()).isEqualTo("RECOVER");
        assertThat(adminEvent.blog().accessMode()).isEqualTo(1);
        assertThat(adminEvent.revision().sensitiveContent()).containsExactly(sensitive);
    }

    @Test
    void deletionUsesIndependentPublicAndAdministrationRevisions() {
        BlogWrapper writer = wrapper(new BlogMaintenanceMode(false));
        Blog blog = blog();
        BlogIdentity identity =
            new BlogIdentity(
                7L,
                42L,
                LocalDateTime.of(2026, 8, 15, 10, 0),
                0L,
                1L,
                LocalDateTime.of(2026, 8, 15, 11, 0),
                null,
                BlogAccessMode.PUBLIC,
                1L,
                5L);
        when(blogs.deleteByIdAndRevisions(7L, 1L, 5L)).thenReturn(1);
        List<Object> messages = captureEvents();

        writer.deleteByIds(
            List.of(new BlogDeletionTarget(blog, identity)),
            new BlogEventContext(BlogOperateEnum.REMOVE, 42L));

        verify(outbox, org.mockito.Mockito.times(2))
            .enqueue(
                eq(OutboxProducer.BLOG),
                eq("BLOG"),
                eq(7L),
                any());
        BlogChangedMessage publicEvent =
            messages.stream()
                .filter(BlogChangedMessage.class::isInstance)
                .map(BlogChangedMessage.class::cast)
                .findFirst()
                .orElseThrow();
        BlogAdminIndexMessage adminEvent =
            messages.stream()
                .filter(BlogAdminIndexMessage.class::isInstance)
                .map(BlogAdminIndexMessage.class::cast)
                .findFirst()
                .orElseThrow();

        assertThat(publicEvent.blogSnapshot().revision()).isEqualTo(2L);
        assertThat(adminEvent.blog().adminRevision()).isEqualTo(6L);
    }

    private List<Object> captureEvents() {
        List<Object> messages = new ArrayList<>();
        when(outbox.enqueue(
                eq(OutboxProducer.BLOG),
                anyString(),
                any(),
                any()))
            .thenAnswer(
                invocation -> {
                    Function<String, Object> factory =
                        invocation.getArgument(3);
                    messages.add(factory.apply("event"));
                    return "event";
                });
        return messages;
    }

    private BlogWrapper wrapper(BlogMaintenanceMode mode) {
        return new BlogWrapper(
            blogs,
            revisions,
            drafts,
            draftSensitive,
            revisionSensitive,
            outbox,
            mode);
    }

    private Blog blog() {
        return BlogFixtures.builder()
            .id(7L)
            .userId(42L)
            .title("title")
            .description("description")
            .content("content")
            .created(LocalDateTime.of(2026, 8, 15, 10, 0))
            .updated(LocalDateTime.of(2026, 8, 15, 11, 0))
            .status(0)
            .link("link")
            .readCount(0L)
            .eventRevision(2L)
            .build();
    }

    private BlogIdentity identity() {
        return new BlogIdentity(
            7L,
            42L,
            LocalDateTime.of(2026, 8, 15, 10, 0),
            0L,
            1L,
            LocalDateTime.of(2026, 8, 15, 11, 0),
            null,
            BlogAccessMode.PUBLIC,
            2L,
            2L);
    }
}
