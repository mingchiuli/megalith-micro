package org.chiu.micro.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = {"org.chiu.micro.search.**", "org.chiu.micro.common.**"})
public class MicroSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroSearchApplication.class, args);
	}

}
