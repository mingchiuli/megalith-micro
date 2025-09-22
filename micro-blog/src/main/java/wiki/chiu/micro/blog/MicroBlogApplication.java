package wiki.chiu.micro.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import wiki.chiu.micro.blog.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroBlogApplication {
    void main(String[] args) {
        SpringApplication.run(MicroBlogApplication.class, args);
    }
}
