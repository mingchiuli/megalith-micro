package org.chiu.micro.exhibit;

import org.chiu.micro.exhibit.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = {"org.chiu.micro.exhibit.**", "org.chiu.micro.common.**"})
@EnableScheduling
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroExhibitApplication {

	public static void main(String[] args) {
			SpringApplication.run(MicroExhibitApplication.class, args);
	}

}
