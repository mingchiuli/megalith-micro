package wiki.chiu.micro.common.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.arch.ApplicationLayers;

class OutboxPackageStructureTest {

    private static final JavaClasses CLASSES =
        ApplicationLayers.productionClasses("wiki.chiu.micro.common.outbox");

    @Test
    void followsTheSharedApplicationLayout() {
        ApplicationLayers.verify(CLASSES, "wiki.chiu.micro.common.outbox");
    }

    @Test
    void everyOutboxClassUsesTheCommonNamespace() {
        assertThat(CLASSES.stream().map(JavaClass::getPackageName))
            .isNotEmpty()
            .allMatch(name -> name.startsWith("wiki.chiu.micro.common.outbox"));
    }
}
