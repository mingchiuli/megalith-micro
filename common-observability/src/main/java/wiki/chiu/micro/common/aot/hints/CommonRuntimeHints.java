package wiki.chiu.micro.common.aot.hints;

import java.util.List;

import javax.net.ssl.SSLParameters;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

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

        hints
            .reflection()
            .registerType(
                SSLParameters.class,
                typeHint ->
                    typeHint
                        .onReachableType(TypeReference.of("org.apache.hc.core5.http2.ssl.H2TlsSupport"))
                        .withMethod(
                            "setEnableRetransmissions",
                            TypeReference.listOf(boolean.class),
                            ExecutableMode.INVOKE));
    }
}
