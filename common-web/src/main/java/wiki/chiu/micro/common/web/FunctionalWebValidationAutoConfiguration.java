package wiki.chiu.micro.common.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FunctionalWebValidationAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  ValidatedRequest validatedRequest() {
    return new ValidatedRequest();
  }
}
