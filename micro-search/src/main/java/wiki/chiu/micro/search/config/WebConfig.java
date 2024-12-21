package wiki.chiu.micro.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wiki.chiu.micro.common.user.AuthArgumentResolver;
import wiki.chiu.micro.common.rpc.AuthHttpService;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthHttpService authHttpService;

    public WebConfig(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthArgumentResolver(authHttpService));
    }

}