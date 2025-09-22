package wiki.chiu.micro.exhibit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;
import wiki.chiu.micro.exhibit.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@EnableScheduling
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroExhibitApplication {
    void main(String[] args) {
        SpringApplication.run(MicroExhibitApplication.class, args);
    }
}
