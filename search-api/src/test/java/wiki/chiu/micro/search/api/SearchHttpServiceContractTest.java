package wiki.chiu.micro.search.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.PostExchange;

class SearchHttpServiceContractTest {

  @Test
  void viewMutationUsesTheSameResourceShapeAsBlogApi() throws NoSuchMethodException {
    var method = SearchHttpService.class.getMethod("addReadCount", Long.class);

    assertThat(method.getAnnotation(PostExchange.class).value()).isEqualTo("/blog/{blogId}/views");
  }
}
