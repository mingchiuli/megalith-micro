package org.chiu.micro.websocket;

import org.chiu.micro.websocket.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class }, proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
@EnableWebSocketSecurity
@EnableWebSocketMessageBroker
public class MicroWebSocketApplication {

	public static void main(String[] args) {
				SpringApplication.run(MicroWebSocketApplication.class, args);
	}

}
