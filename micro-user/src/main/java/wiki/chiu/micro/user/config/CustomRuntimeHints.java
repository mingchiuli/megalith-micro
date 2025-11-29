package wiki.chiu.micro.user.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection

        hints.resources()
                .registerPattern("ValidationMessages.properties")
                .registerPattern("logback-spring.xml");

    }
}
