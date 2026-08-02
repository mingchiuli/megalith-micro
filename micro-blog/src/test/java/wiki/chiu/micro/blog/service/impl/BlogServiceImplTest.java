package wiki.chiu.micro.blog.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.rpc.SearchHttpServiceWrapper;
import wiki.chiu.micro.blog.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.blog.service.BlogAccessPolicy;
import wiki.chiu.micro.blog.wrapper.BlogWrapper;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.OssHttpService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BlogServiceImplTest {

    @Test
    void newBlogIsOwnedAndManageableByCurrentUser() {
        BlogServiceImpl service = new BlogServiceImpl(
                mock(UserHttpServiceWrapper.class),
                mock(OssHttpService.class),
                mock(ApplicationContext.class),
                mock(BlogRepository.class),
                mock(StringRedisTemplate.class),
                mock(ResourceLoader.class),
                mock(BlogWrapper.class),
                mock(BlogSensitiveContentRepository.class),
                mock(SearchHttpServiceWrapper.class),
                mock(TaskExecutor.class),
                mock(JsonMapper.class),
                new BlogAccessPolicy("admin"),
                mock(AuthHttpService.class));

        var edit = service.findEdit(null, 42L, List.of("user"));

        assertEquals(42L, edit.userId());
        assertTrue(edit.permissions().collaborate());
        assertTrue(edit.permissions().commit());
        assertTrue(edit.permissions().manageMetadata());
        assertTrue(edit.permissions().manageAssets());
    }
}
