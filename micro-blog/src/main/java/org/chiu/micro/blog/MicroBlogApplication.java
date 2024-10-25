package org.chiu.micro.blog;

import org.chiu.micro.blog.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaAuditing
@EnableAsync
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroBlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroBlogApplication.class, args);
	}

}
