package org.chiu.micro.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(proxyBeanMethods = false)
public class MicroSearchApplication {

	public static void main(String[] args) {
				SpringApplication.run(MicroSearchApplication.class, args);
		}

}
