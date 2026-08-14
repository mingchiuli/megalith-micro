package wiki.chiu.micro.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import wiki.chiu.micro.search.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints(CustomRuntimeHints.class)
public class MicroSearchApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroSearchApplication.class, args);
  }
}
