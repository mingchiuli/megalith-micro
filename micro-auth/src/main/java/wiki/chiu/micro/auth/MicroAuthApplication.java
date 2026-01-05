package wiki.chiu.micro.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.auth.config.CustomRuntimeHints;
import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
@ImportHttpServices(group = "sms", types = {SmsHttpService.class})
@ImportHttpServices(group = "user", types = {UserHttpService.class, MenuHttpService.class, AuthorityHttpService.class})
public class MicroAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroAuthApplication.class, args);
    }
}
