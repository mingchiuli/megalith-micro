package wiki.chiu.micro.common.rpc.config.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

class AuthHttpInterceptorTest {

  private final AuthHttpInterceptor interceptor = new AuthHttpInterceptor();

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void propagatesPrincipalFromTheCurrentRequest() throws Exception {
    bindInboundPrincipal("encoded-principal");
    var outbound = new MockClientHttpRequest();

    interceptor.intercept(outbound, new byte[0], this::respondOk);

    assertThat(outbound.getHeaders().getFirst(InternalHttpHeaders.PRINCIPAL))
        .isEqualTo("encoded-principal");
  }

  @Test
  void doesNotOverwriteAnExplicitPrincipalHeader() throws Exception {
    bindInboundPrincipal("inbound-principal");
    var outbound = new MockClientHttpRequest();
    outbound.getHeaders().set(InternalHttpHeaders.PRINCIPAL, "explicit-principal");

    interceptor.intercept(outbound, new byte[0], this::respondOk);

    assertThat(outbound.getHeaders().getFirst(InternalHttpHeaders.PRINCIPAL))
        .isEqualTo("explicit-principal");
  }

  @Test
  void proceedsWithoutPrincipalWhenThereIsNoRequestContext() {
    var outbound = new MockClientHttpRequest();

    assertThatCode(() -> interceptor.intercept(outbound, new byte[0], this::respondOk))
        .doesNotThrowAnyException();
    assertThat(outbound.getHeaders().getFirst(InternalHttpHeaders.PRINCIPAL)).isNull();
  }

  private void bindInboundPrincipal(String principal) {
    var request = new MockHttpServletRequest();
    request.addHeader(InternalHttpHeaders.PRINCIPAL, principal);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  private MockClientHttpResponse respondOk(
      org.springframework.http.HttpRequest request, byte[] body) {
    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
  }
}
