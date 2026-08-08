package wiki.chiu.micro.exhibit.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.redisson.api.RScript.Mode.READ_WRITE;
import static org.redisson.api.RScript.ReturnType.BOOLEAN;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;

class BlogServiceImplTest {

  private final BlogWrapper blogWrapper = mock(BlogWrapper.class);
  private final RedissonClient redissonClient = mock(RedissonClient.class);
  private final RScript script = mock(RScript.class);
  private BlogServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new BlogServiceImpl(
            mock(BlogSensitiveWrapper.class),
            mock(BlogHttpServiceWrapper.class),
            redissonClient,
            blogWrapper,
            mock(ResourceLoader.class));
    ReflectionTestUtils.setField(service, "consumeReadTokenScript", "compare-delete");
    when(redissonClient.getScript()).thenReturn(script);
  }

  @Test
  void consumesTokenAtomicallyBeforeReturningBlog() {
    when(script.eval(
            eq(READ_WRITE),
            eq("compare-delete"),
            eq(BOOLEAN),
            eq(List.of("read_token:7")),
            eq("token")))
        .thenReturn(true);
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
    verify(blogWrapper).setReadCount(7L);
    verify(script)
        .eval(
            eq(READ_WRITE),
            eq("compare-delete"),
            eq(BOOLEAN),
            eq(List.of("read_token:7")),
            eq("token"));
  }

  @Test
  void rejectsTokenThatWasNotConsumed() {
    when(script.eval(
            eq(READ_WRITE), eq("compare-delete"), eq(BOOLEAN), eq(List.of("read_token:7")), any()))
        .thenReturn(false);

    assertThrows(MissException.class, () -> service.getLockedBlog(7L, "wrong"));
    verify(blogWrapper, never()).findById(7L);
  }
}
