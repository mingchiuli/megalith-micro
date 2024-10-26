package wiki.chiu.micro.websocket;

import wiki.chiu.micro.websocket.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroWebSocketApplication {

	public static void main(String[] args) {
				SpringApplication.run(MicroWebSocketApplication.class, args);
	}

}
