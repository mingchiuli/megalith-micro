package wiki.chiu.micro.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.service.registry.ImportHttpServices;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.search.config.CustomRuntimeHints;


@SpringBootApplication(proxyBeanMethods = false)
@ImportHttpServices(group = "auth", types = {AuthHttpService.class})
@ImportHttpServices(group = "blog", types = {BlogHttpService.class})
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroSearchApplication {

    public static void main(String[] args) {
		SpringApplication.run(MicroSearchApplication.class, args);
	}

}
