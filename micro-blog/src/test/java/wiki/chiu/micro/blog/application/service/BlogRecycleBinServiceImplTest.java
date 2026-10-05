package wiki.chiu.micro.blog.application.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogSnapshot;

class BlogRecycleBinServiceImplTest {

    private final BlogRuntimeStore runtimeStore = mock(BlogRuntimeStore.class);
    private final BlogRecycleBinServiceImpl service = new BlogRecycleBinServiceImpl(runtimeStore);

    @Test
    void removedBlogByItsOwnerIsParkedInTheRecycleBin() {
        BlogSnapshot snapshot = snapshot();
        BlogChangedMessage event =
            new BlogChangedMessage("event-7", BlogOperateEnum.REMOVE.getCode(), 3L, 42L, snapshot);

        service.recycle(event);

        verify(runtimeStore).saveDeletedBlog(42L, "event-7", snapshot);
    }

    @Test
    void cascadeDeleteWithoutOperatorIsIgnored() {
        BlogChangedMessage event =
            new BlogChangedMessage("event-8", BlogOperateEnum.REMOVE.getCode(), 3L, null, snapshot());

        service.recycle(event);

        verifyNoInteractions(runtimeStore);
    }

    @Test
    void nonRemoveEventsAreIgnored() {
        BlogChangedMessage event =
            new BlogChangedMessage("event-9", BlogOperateEnum.UPDATE.getCode(), 3L, 42L, snapshot());

        service.recycle(event);

        verifyNoInteractions(runtimeStore);
    }

    private BlogSnapshot snapshot() {
        LocalDateTime now = LocalDateTime.now();
        return new BlogSnapshot(9L, 42L, "title", "description", "content", now, now, 0, null, 1L, 3L);
    }
}
