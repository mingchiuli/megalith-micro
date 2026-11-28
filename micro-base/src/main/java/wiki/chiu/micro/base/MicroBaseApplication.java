package wiki.chiu.micro.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.blog.adapter.out.http.auth.AuthHttpService;
import wiki.chiu.micro.blog.adapter.out.http.search.SearchHttpService;
import wiki.chiu.micro.blog.config.CustomRuntimeHints;
import wiki.chiu.micro.common.rpc.OssHttpService;

@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = {"wiki.chiu.micro.user", "wiki.chiu.micro.blog", "wiki.chiu.micro.base"})
@EntityScan(basePackages = {"wiki.chiu.micro.user", "wiki.chiu.micro.blog", "wiki.chiu.micro.common.outbox"})
@EnableJpaRepositories(basePackages = {"wiki.chiu.micro.user", "wiki.chiu.micro.blog", "wiki.chiu.micro.common.outbox"})
@EnableJpaAuditing
@ImportRuntimeHints({CustomRuntimeHints.class, wiki.chiu.micro.user.config.CustomRuntimeHints.class})
@ImportHttpServices(
    group = "oss",
    types = {OssHttpService.class})
@ImportHttpServices(
    group = "auth",
    types = {AuthHttpService.class})
@ImportHttpServices(
    group = "search",
    types = {SearchHttpService.class})
public class MicroBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroBaseApplication.class, args);
    }
}
