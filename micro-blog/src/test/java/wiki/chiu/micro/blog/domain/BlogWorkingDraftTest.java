package wiki.chiu.micro.blog.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;

class BlogWorkingDraftTest {

    private static final LocalDateTime PUBLISHED = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    void activeDraftMustBeBasedOnTheCurrentPublicationAndBeNewer() {
        BlogIdentity published = identity(21L);

        assertTrue(draft(21L, PUBLISHED.plusMinutes(1)).newerThan(published));
        assertFalse(draft(20L, PUBLISHED.plusMinutes(1)).newerThan(published));
        assertFalse(draft(21L, PUBLISHED.minusMinutes(1)).newerThan(published));
    }

    private BlogIdentity identity(Long publishedRevisionId) {
        return new BlogIdentity(
            7L,
            42L,
            PUBLISHED.minusDays(1),
            0L,
            publishedRevisionId,
            PUBLISHED,
            null,
            BlogAccessMode.PUBLIC,
            1L,
            2L);
    }

    private BlogWorkingDraft draft(Long baseRevisionId, LocalDateTime updatedAt) {
        return new BlogWorkingDraft(
            1L,
            7L,
            3L,
            baseRevisionId,
            "title",
            "description",
            "content",
            "",
            BlogRenderMode.NORMAL,
            42L,
            updatedAt,
            List.of());
    }
}
