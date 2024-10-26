package wiki.chiu.micro.user;

import wiki.chiu.micro.user.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@ImportRuntimeHints({ CustomRuntimeHints.class })
@EnableAsync
public class MicroUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroUserApplication.class, args);
	}

}
