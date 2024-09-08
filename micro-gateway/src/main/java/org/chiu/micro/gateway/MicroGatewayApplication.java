package org.chiu.micro.gateway;

import org.chiu.micro.gateway.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroGatewayApplication {

		public static void main(String[] args) {
				SpringApplication.run(MicroGatewayApplication.class, args);
		}

}
