package wiki.chiu.micro.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import wiki.chiu.micro.user.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroUserApplication {

   static void main(String[] args) {
        SpringApplication.run(MicroUserApplication.class, args);
    }
}
