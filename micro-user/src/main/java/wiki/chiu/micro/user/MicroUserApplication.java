package wiki.chiu.micro.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.user.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@ImportRuntimeHints({ CustomRuntimeHints.class })
@ImportHttpServices(group = "oss", types = {OssHttpService.class})
@ImportHttpServices(group = "auth", types = {AuthHttpService.class})
public class MicroUserApplication {

   public static void main(String[] args) {
        SpringApplication.run(MicroUserApplication.class, args);
    }
}
