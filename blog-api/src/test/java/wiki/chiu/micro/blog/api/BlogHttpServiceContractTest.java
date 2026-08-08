package wiki.chiu.micro.blog.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

class BlogHttpServiceContractTest {

  @Test
  void pageQueryUsesGet() throws NoSuchMethodException {
    var method = BlogHttpService.class.getMethod("findPage", Integer.class, Integer.class);

    assertThat(method.getAnnotation(GetExchange.class).value()).isEqualTo("/blog/page");
    assertThat(method.getAnnotation(PostExchange.class)).isNull();
  }

  @Test
  void viewMutationHasAnExplicitResourcePath() throws NoSuchMethodException {
    var method = BlogHttpService.class.getMethod("setReadCount", Long.class);

    assertThat(method.getAnnotation(PostExchange.class).value()).isEqualTo("/blog/{blogId}/views");
  }
}
