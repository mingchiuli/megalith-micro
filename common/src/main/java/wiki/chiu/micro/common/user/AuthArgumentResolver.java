package wiki.chiu.micro.common.user;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthHttpService;

import java.util.Optional;


public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthHttpService authHttpService;

    public AuthArgumentResolver(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthInfo.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String token = Optional.ofNullable(webRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .orElse("");
        AuthRpcDto authRpcDto = Result.handleResult(() -> authHttpService.getAuthentication(token));
        return AuthInfo.builder()
                .userId(authRpcDto.userId())
                .roles(authRpcDto.roles())
                .authorities(authRpcDto.authorities())
                .build();
    }
}
