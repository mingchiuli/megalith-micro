package wiki.chiu.micro.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.blog.config.CustomRuntimeHints;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.user.api.UserHttpService;

// An explicit @EntityScan/@EnableJpaRepositories replaces (not merges) default base-package
// scanning, so future entity/repository packages under this app must be listed here too.
@SpringBootApplication(proxyBeanMethods = false)
@EntityScan(basePackages = {"wiki.chiu.micro.blog", "wiki.chiu.micro.outbox"})
@EnableJpaRepositories(basePackages = {"wiki.chiu.micro.blog", "wiki.chiu.micro.outbox"})
@EnableJpaAuditing
@ImportRuntimeHints({CustomRuntimeHints.class})
@ImportHttpServices(
    group = "user",
    types = {UserHttpService.class})
@ImportHttpServices(
    group = "oss",
    types = {OssHttpService.class})
@ImportHttpServices(
    group = "auth",
    types = {AuthHttpService.class})
@ImportHttpServices(
    group = "search",
    types = {SearchHttpService.class})
public class MicroBlogApplication {
  public static void main(String[] args) {
    SpringApplication.run(MicroBlogApplication.class, args);
  }
}
