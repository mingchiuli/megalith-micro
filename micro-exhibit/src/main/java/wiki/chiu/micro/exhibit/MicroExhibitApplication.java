package wiki.chiu.micro.exhibit;

import wiki.chiu.micro.exhibit.config.CustomRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(proxyBeanMethods = false)
@EnableScheduling
@ImportRuntimeHints({ CustomRuntimeHints.class })
public class MicroExhibitApplication {

	public static void main(String[] args) {
			SpringApplication.run(MicroExhibitApplication.class, args);
	}

}
