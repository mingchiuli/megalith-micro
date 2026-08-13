package wiki.chiu.micro.common.rpc.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthHttpInterceptor implements ClientHttpRequestInterceptor {

  private static final String PRINCIPAL_HEADER = "X-Megalith-Principal";
  private static final Logger log = LoggerFactory.getLogger(AuthHttpInterceptor.class);

  @Override
  public @NonNull ClientHttpResponse intercept(
      @NonNull HttpRequest request,
      byte @NonNull [] body,
      @NonNull ClientHttpRequestExecution execution)
      throws IOException {
    try {
      HttpServletRequest req =
          ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes()))
              .getRequest();
      String principal = req.getHeader(PRINCIPAL_HEADER);
      if (principal != null && !principal.isBlank()) {
        request.getHeaders().putIfAbsent(PRINCIPAL_HEADER, List.of(principal));
      }
    } catch (IllegalStateException e) {
      log.debug("Request context not available, proceeding without principal propagation", e);
    }

    return execution.execute(request, body);
  }
}
