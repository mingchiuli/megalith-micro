package wiki.chiu.micro.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.blog.api.BlogIndexSourceHttpService;
import wiki.chiu.micro.search.config.SearchRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints(SearchRuntimeHints.class)
@ImportHttpServices(group = "blog", types = BlogIndexSourceHttpService.class)
public class MicroSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroSearchApplication.class, args);
    }
}
