package org.chiu.micro.auth;

import org.chiu.micro.auth.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroAuthApplication {

		public static void main(String[] args) {
				SpringApplication.run(MicroAuthApplication.class, args);
		}

}
