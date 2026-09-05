package wiki.chiu.micro.exhibit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.blog.api.BlogHttpService;
import wiki.chiu.micro.exhibit.config.CustomRuntimeHints;
import wiki.chiu.micro.user.api.UserHttpService;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({CustomRuntimeHints.class})
@EnableScheduling
@ImportHttpServices(
    group = "user",
    types = {UserHttpService.class})
@ImportHttpServices(
    group = "blog",
    types = {BlogHttpService.class})
public class MicroExhibitApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroExhibitApplication.class, args);
    }
}
