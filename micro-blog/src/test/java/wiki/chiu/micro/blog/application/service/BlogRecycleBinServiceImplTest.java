package wiki.chiu.micro.blog.application.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.model.RecycleBlogCommand;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogOperateEnum;

class BlogRecycleBinServiceImplTest {

    private final BlogRuntimeStore runtimeStore = mock(BlogRuntimeStore.class);
    private final BlogRecycleBinServiceImpl service = new BlogRecycleBinServiceImpl(runtimeStore);

    @Test
    void removedBlogByItsOwnerIsParkedInTheRecycleBin() {
        Blog blog = blog();
        service.recycle(new RecycleBlogCommand("event-7", BlogOperateEnum.REMOVE, 42L, blog));
        verify(runtimeStore).saveDeletedBlog(42L, "event-7", blog);
    }

    @Test
    void cascadeDeleteWithoutOperatorIsIgnored() {
        service.recycle(new RecycleBlogCommand("event-8", BlogOperateEnum.REMOVE, null, null));
        verifyNoInteractions(runtimeStore);
    }

    @Test
    void nonRemoveEventsAreIgnored() {
        service.recycle(new RecycleBlogCommand("event-9", BlogOperateEnum.UPDATE, 42L, null));
        verifyNoInteractions(runtimeStore);
    }

    private Blog blog() {
        LocalDateTime now = LocalDateTime.now();
        return new Blog(9L, 42L, "title", "description", "content", now, now, 0, null, 1L, 3L);
    }
}
