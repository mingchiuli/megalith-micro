package wiki.chiu.micro.blog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

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

  @Test
  void createdQueryUsesIsoDateTime() {
    var restClientBuilder = RestClient.builder().baseUrl("https://blog.test");
    var server = MockRestServiceServer.bindTo(restClientBuilder).build();
    var client =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClientBuilder.build()))
            .build()
            .createClient(BlogHttpService.class);

    server
        .expect(requestTo("https://blog.test/blog/count/until?created=2026-08-01T12%3A04%3A00"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess());

    var result = client.countByCreatedGreaterThanEqual(LocalDateTime.of(2026, 8, 1, 12, 4));

    assertThat(result).isNull();
    server.verify();
  }
}
