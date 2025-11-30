package wiki.chiu.micro.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;

@SpringBootApplication(proxyBeanMethods = false)
@ImportHttpServices(group = "sms", types = {SmsHttpService.class})
@ImportHttpServices(group = "user", types = {UserHttpService.class, MenuHttpService.class, AuthorityHttpService.class})
@EnableScheduling
public class MicroAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroAuthApplication.class, args);
    }
}
