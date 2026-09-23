package wiki.chiu.micro.cache;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.Test;

class CachePackageStructureTest {

    private static final JavaClasses CLASSES =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("wiki.chiu.micro.cache");

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
                "wiki.chiu.micro.cache.aot..",
                "wiki.chiu.micro.cache.config..",
                "wiki.chiu.micro.cache.metrics..")
            .check(CLASSES);
    }

    @Test
    void applicationLayerStaysFreeOfAdaptersAndInfrastructure() {
        noClasses()
            .that()
            .resideInAPackage("wiki.chiu.micro.cache.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "wiki.chiu.micro.cache.adapter..",
                "wiki.chiu.micro.cache.config..",
                "org.redisson..",
                "org.springframework..",
                "com.github.benmanes.caffeine..")
            .check(CLASSES);
    }

    @Test
    void inboundEvictionListenersDoNotDependOnOutboundEvictionTransports() {
        noClasses()
            .that()
            .resideInAPackage("wiki.chiu.micro.cache.adapter.in.messaging..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("wiki.chiu.micro.cache.adapter.out..")
            .check(CLASSES);
    }

    @Test
    void everyCacheClassUsesTheCacheNamespace() {
        assertThat(CLASSES.stream().map(JavaClass::getPackageName))
            .isNotEmpty()
            .allMatch(name -> name.startsWith("wiki.chiu.micro.cache"));
    }
}
