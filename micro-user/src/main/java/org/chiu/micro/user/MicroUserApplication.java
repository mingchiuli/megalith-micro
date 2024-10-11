package org.chiu.micro.user;

import org.chiu.micro.user.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroUserApplication.class, args);
	}

}
