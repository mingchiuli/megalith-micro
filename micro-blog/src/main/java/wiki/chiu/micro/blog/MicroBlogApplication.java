package wiki.chiu.micro.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.blog.config.CustomRuntimeHints;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@ImportRuntimeHints({ CustomRuntimeHints.class })
@ImportHttpServices(group = "user", types = {UserHttpService.class})
@ImportHttpServices(group = "oss", types = {OssHttpService.class})
@ImportHttpServices(group = "auth", types = {AuthHttpService.class})
@ImportHttpServices(group = "search", types = {SearchHttpService.class})
public class MicroBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroBlogApplication.class, args);
    }
}
