package wiki.chiu.micro.user.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PasswordLockProperties.class)
public class PasswordLockConfig {}
