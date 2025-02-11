package wiki.chiu.micro.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import wiki.chiu.micro.websocket.config.CustomRuntimeHints;

@SpringBootApplication(proxyBeanMethods = false)
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroWebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroWebSocketApplication.class, args);
    }
}
