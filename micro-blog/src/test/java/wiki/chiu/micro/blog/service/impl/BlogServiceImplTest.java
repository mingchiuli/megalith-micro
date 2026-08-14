package wiki.chiu.micro.blog.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.service.BlogAccessPolicy;
import wiki.chiu.micro.blog.service.port.BlogSearchGateway;
import wiki.chiu.micro.blog.service.port.CollaborationTicketGateway;
import wiki.chiu.micro.blog.wrapper.BlogWrapper;

class BlogServiceImplTest {

  @Test
  void newBlogIsOwnedAndManageableByCurrentUser() {
    BlogServiceImpl service =
        new BlogServiceImpl(
            mock(BlogRepository.class),
            mock(StringRedisTemplate.class),
            mock(ResourceLoader.class),
            mock(BlogWrapper.class),
            mock(BlogSensitiveContentRepository.class),
            mock(BlogSearchGateway.class),
            mock(JsonMapper.class),
            new BlogAccessPolicy());

    var edit = service.findEdit(null, 42L, List.of());

    assertEquals(42L, edit.userId());
    assertTrue(edit.permissions().collaborate());
    assertTrue(edit.permissions().commit());
    assertTrue(edit.permissions().manageMetadata());
    assertTrue(edit.permissions().manageAssets());
  }

  @Test
  void returnsOnlyTheOneTimeReadToken() {
    BlogRepository blogRepository = mock(BlogRepository.class);
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(values);
    when(blogRepository.findById(7L))
        .thenReturn(Optional.of(BlogEntity.builder().id(7L).userId(42L).build()));
    BlogCollaborationServiceImpl service =
        new BlogCollaborationServiceImpl(
            blogRepository,
            new BlogAccessPolicy(),
            redisTemplate,
            mock(CollaborationTicketGateway.class));

    String token = service.issueReadToken(7L, 42L, List.of());

    assertFalse(token.contains("?token="));
    assertFalse(token.contains("/blog/"));
    verify(values).set(eq("read_token:7"), eq(token), any(Expiration.class));
  }
}
