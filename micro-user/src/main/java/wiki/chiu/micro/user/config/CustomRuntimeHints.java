package wiki.chiu.micro.user.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // ValidationMessages.properties for Bean Validation
        hints.resources()
                .registerPattern("ValidationMessages.properties");
    }
}
