package wiki.chiu.micro.auth.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import wiki.chiu.micro.auth.dto.AuthDto;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.user.AuthInfo;

import java.util.Optional;


public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public AuthArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthInfo.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws AuthException {
        String token = Optional.ofNullable(webRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .orElse("");

        AuthDto authDto = authService.getAuthDto(token);
        return AuthInfo.builder()
                .userId(authDto.userId())
                .roles(authDto.roles())
                .authorities(authDto.authorities())
                .build();
    }
}
