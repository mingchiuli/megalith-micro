package org.chiu.micro.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = {"org.chiu.micro.gateway.**", "org.chiu.micro.common.**"})
public class MicroGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroGatewayApplication.class, args);
	}
}
