package wiki.chiu.micro.blog.support;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;

import java.util.List;

public class StubAuthInfoResolver implements HandlerMethodArgumentResolver {

    private final AuthInfo authInfo;

    public StubAuthInfoResolver(Long userId, List<String> roles) {
        this.authInfo = AuthInfo.builder()
                .userId(userId)
                .roles(roles)
                .authorities(List.of())
                .build();
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthInfo.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return authInfo;
    }
}
