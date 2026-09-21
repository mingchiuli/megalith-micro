package wiki.chiu.micro.common.observability.aot.hints;

import java.util.List;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

class CommonRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // Protobuf discovers the full registry reflectively when OTLP metrics initializes.
        hints
            .reflection()
            .registerTypeIfPresent(
                classLoader,
                "com.google.protobuf.ExtensionRegistry",
                typeHint -> typeHint.withMethod("getEmptyRegistry", List.of(), ExecutableMode.INVOKE));
    }
}
