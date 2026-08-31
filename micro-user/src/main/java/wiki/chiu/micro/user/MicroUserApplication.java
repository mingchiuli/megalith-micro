package wiki.chiu.micro.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.user.config.CustomRuntimeHints;

// An explicit @EntityScan/@EnableJpaRepositories replaces (not merges) default base-package
// scanning, so future entity/repository packages under this app must be listed here too.
@SpringBootApplication(proxyBeanMethods = false)
@EntityScan(basePackages = {"wiki.chiu.micro.user", "wiki.chiu.micro.outbox"})
@EnableJpaRepositories(basePackages = {"wiki.chiu.micro.user", "wiki.chiu.micro.outbox"})
@EnableJpaAuditing
@ImportRuntimeHints({CustomRuntimeHints.class})
@ImportHttpServices(
    group = "oss",
    types = {OssHttpService.class})
public class MicroUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroUserApplication.class, args);
    }
}
