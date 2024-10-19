package org.chiu.micro.websocket;

import org.chiu.micro.websocket.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = {"org.chiu.micro.websocket.**", "org.chiu.micro.common.**"})
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroWebSocketApplication {

	public static void main(String[] args) {
				SpringApplication.run(MicroWebSocketApplication.class, args);
	}

}
