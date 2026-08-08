package wiki.chiu.micro.common.rpc.config;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.auth.AuthArgumentResolver;

@AutoConfiguration
public class AuthArgumentResolverConfig {

  @Bean
  public WebMvcConfigurer webMvcConfigurer(
      ObjectProvider<@NonNull AuthHttpService> authHttpServiceProvider) {
    return new WebMvcConfigurer() {
      @Override
      public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        AuthHttpService authHttpService = authHttpServiceProvider.getIfAvailable();
        if (authHttpService != null) {
          resolvers.add(new AuthArgumentResolver(authHttpService));
        }
      }
    };
  }
}
