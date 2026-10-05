package wiki.chiu.micro.search.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.arch.ApplicationLayers;

class LayeringArchitectureTest {

    private static final String ROOT = "wiki.chiu.micro.search";

    @Test
    void applicationLayersAreRespected() {
        ApplicationLayers.verify(ROOT);
    }

    @Test
    void outputPortsAreOwnedByTheApplication() {
        noClasses()
            .that()
            .resideInAPackage("..application.port.out..")
            .should()
            .dependOnClassesThat()
            .resideOutsideOfPackages("..domain..", "..application..", "java..")
            .check(ApplicationLayers.productionClasses(ROOT));
    }
}
