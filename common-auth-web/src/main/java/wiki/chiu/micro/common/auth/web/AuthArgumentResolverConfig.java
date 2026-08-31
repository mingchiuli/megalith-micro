package wiki.chiu.micro.common.auth.web;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.security.AuthPrincipal;

@AutoConfiguration
@RegisterReflectionForBinding({AuthPrincipal.class, DataPermissionEnum.class})
public class AuthArgumentResolverConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new AuthArgumentResolver());
            }
        };
    }
}
