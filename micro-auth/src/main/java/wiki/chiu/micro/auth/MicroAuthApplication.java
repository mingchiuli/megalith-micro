package wiki.chiu.micro.auth;

import wiki.chiu.micro.auth.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
@EnableScheduling
public class MicroAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroAuthApplication.class, args);
	}

}
