package wiki.chiu.micro.common.web;

import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration")
@ConditionalOnClass(Validator.class)
@ConditionalOnBean(Validator.class)
public class FunctionalWebValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ValidatedRequest validatedRequest(Validator validator) {
        return new ValidatedRequest(validator);
    }
}
