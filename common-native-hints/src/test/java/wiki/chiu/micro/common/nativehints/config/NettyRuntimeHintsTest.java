package wiki.chiu.micro.common.nativehints.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;

class NettyRuntimeHintsTest {

    @Test
    void aotFactoriesDiscoverTheRegistrarAndRegisterOnlyIntrospectionTypes() {
        ClassLoader loader = getClass().getClassLoader();
        var registrars = SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories", loader)
            .load(RuntimeHintsRegistrar.class);
        var registrar = registrars.stream().filter(NettyRuntimeHints.class::isInstance).findFirst().orElseThrow();
        RuntimeHints hints = new RuntimeHints();

        registrar.registerHints(hints, loader);

        assertThat(hints.reflection().typeHints()).hasSize(2);
        for (String name : List.of(
            "io.netty.util.ReferenceCountUtil", "io.netty.buffer.AdvancedLeakAwareByteBuf")) {
            var type = hints.reflection().getTypeHint(TypeReference.of(name));
            assertThat(type).isNotNull();
            assertThat(type.methods()).isEmpty();
            assertThat(type.getMemberCategories()).isEmpty();
        }
    }

    @Test
    void doesNotRequireNettyOnTheApplicationClasspath() throws Exception {
        try (var loader = new FilteredClassLoader("io.netty")) {
            RuntimeHints hints = new RuntimeHints();
            new NettyRuntimeHints().registerHints(hints, loader);

            assertThat(hints.reflection().typeHints()).isEmpty();
        }
    }
}
