package wiki.chiu.micro.common.rpc.config.auth;

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.vo.AuthRpcVo;

public record AuthArgumentResolver(AuthHttpService authHttpService)
    implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(@NonNull MethodParameter parameter) {
    return parameter.getParameterType().equals(AuthPrincipal.class);
  }

  @Override
  public Object resolveArgument(
      @NonNull MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    String token = Optional.ofNullable(webRequest.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");
    AuthRpcVo authRpcVo =
        RemoteResult.requireSuccess(() -> authHttpService.getAuthentication(token));
    return AuthPrincipal.builder()
        .userId(authRpcVo.userId())
        .roles(authRpcVo.roles())
        .authorities(authRpcVo.authorities())
        .build();
  }
}
