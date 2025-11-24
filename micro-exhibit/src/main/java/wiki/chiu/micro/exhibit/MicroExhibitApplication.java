package wiki.chiu.micro.exhibit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.common.rpc.*;
import wiki.chiu.micro.exhibit.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@EnableScheduling
@ImportRuntimeHints({ CustomRuntimeHints.class })
@ImportHttpServices(group = "user", types = {UserHttpService.class})
@ImportHttpServices(group = "blog", types = {BlogHttpService.class})
@ImportHttpServices(group = "auth", types = {AuthHttpService.class})
@ImportHttpServices(group = "search", types = {SearchHttpService.class})
public class MicroExhibitApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroExhibitApplication.class, args);
    }
}
