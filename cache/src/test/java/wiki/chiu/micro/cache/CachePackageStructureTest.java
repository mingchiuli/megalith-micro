package wiki.chiu.micro.cache;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import java.util.Set;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.arch.ApplicationLayers;

class CachePackageStructureTest {

    private static final JavaClasses CLASSES =
        ApplicationLayers.productionClasses("wiki.chiu.micro.cache");

    @Test
    void followsTheSharedApplicationLayout() {
        ApplicationLayers.verify(
            CLASSES, "wiki.chiu.micro.cache", Set.of("annotation", "handler", "key"));
    }

    @Test
    void publishedApiStaysIndependentOfInternals() {
        noClasses()
            .that()
            .resideInAnyPackage(
                "wiki.chiu.micro.cache.annotation",
                "wiki.chiu.micro.cache.handler",
                "wiki.chiu.micro.cache.key")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "wiki.chiu.micro.cache.adapter..",
                "wiki.chiu.micro.cache.application..",
                "wiki.chiu.micro.cache.config..")
            .check(CLASSES);
    }

    @Test
    void everyCacheClassUsesTheCacheNamespace() {
        assertThat(CLASSES.stream().map(JavaClass::getPackageName))
            .isNotEmpty()
            .allMatch(name -> name.startsWith("wiki.chiu.micro.cache"));
    }
}
