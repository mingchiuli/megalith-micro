package wiki.chiu.micro.common.outbox;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.Test;

class OutboxPackageStructureTest {

    private static final JavaClasses CLASSES =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("wiki.chiu.micro.common.outbox");

    @Test
    void domainDoesNotDependOnOuterLayers() {
        noClasses()
            .that()
            .resideInAPackage("wiki.chiu.micro.common.outbox.domain")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "wiki.chiu.micro.common.outbox.application..",
                "wiki.chiu.micro.common.outbox.adapter..",
                "wiki.chiu.micro.common.outbox.config..")
            .check(CLASSES);
    }

    @Test
    void applicationModelsStayFreeOfAdaptersAndConfiguration() {
        noClasses()
            .that()
            .resideInAPackage("wiki.chiu.micro.common.outbox.application.model")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "wiki.chiu.micro.common.outbox.adapter..",
                "wiki.chiu.micro.common.outbox.config..")
            .check(CLASSES);
    }

    @Test
    void actuatorEndpointDoesNotDependOnOutboundAdapters() {
        noClasses()
            .that()
            .resideInAPackage("wiki.chiu.micro.common.outbox.adapter.in..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("wiki.chiu.micro.common.outbox.adapter.out..")
            .check(CLASSES);
    }

    @Test
    void everyOutboxClassUsesTheCommonNamespace() {
        assertThat(CLASSES.stream().map(JavaClass::getPackageName))
            .isNotEmpty()
            .allMatch(name -> name.startsWith("wiki.chiu.micro.common.outbox"));
    }
}
