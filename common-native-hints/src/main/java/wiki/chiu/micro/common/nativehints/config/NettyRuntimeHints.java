package wiki.chiu.micro.common.nativehints.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Makes Netty's leak-detector exclusion types available for method introspection. */
public class NettyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // GraalVM metadata overrides netty-buffer's hints. The detector queries method names only.
        hints.reflection()
            .registerTypeIfPresent(classLoader, "io.netty.util.ReferenceCountUtil")
            .registerTypeIfPresent(classLoader, "io.netty.buffer.AdvancedLeakAwareByteBuf");
    }
}
