package wiki.chiu.micro.user.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(CustomRuntimeHints.class);

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.resources().registerPattern("ValidationMessages.properties");

    }
}
