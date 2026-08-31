package wiki.chiu.micro.exhibit.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.Test;

class LayeringArchitectureTest {

    @Test
    void applicationServicesDoNotOwnTransactions() {
        noClasses()
            .that()
            .resideInAPackage("..service..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("org.springframework.transaction..")
            .check(
                new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("wiki.chiu.micro.exhibit"));
    }

    @Test
    void applicationServicesDependOnPortsInsteadOfRpcAdapters() {
        var classes =
            new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("wiki.chiu.micro.exhibit");
        noClasses()
            .that()
            .resideInAPackage("..service..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("wiki.chiu.micro.exhibit.adapter.out..")
            .check(classes);
        noClasses()
            .that()
            .resideInAPackage("..service..")
            .should()
            .dependOnClassesThat()
            .haveSimpleNameEndingWith("HttpService")
            .check(classes);
        noClasses()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..adapter.out..", "..repository..", "..rpc..", "..wrapper..")
            .check(classes);
        noClasses()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .dependOnClassesThat()
            .haveSimpleNameEndingWith("Wrapper")
            .check(classes);
    }

    @Test
    void applicationServicesDoNotDependOnRedisInfrastructure() {
        noClasses()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.redisson..", "org.springframework.data.redis..", "org.springframework.core.io..")
            .check(
                new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("wiki.chiu.micro.exhibit"));
    }

    @Test
    void portsAreFrameworkIndependentAndInputAdaptersUseInputPorts() {
        var classes =
            new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("wiki.chiu.micro.exhibit");
        noClasses()
            .that()
            .resideInAPackage("..application.port..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..adapter..", "org.springframework..", "org.redisson..", "tools.jackson..")
            .check(classes);
        noClasses()
            .that()
            .resideInAPackage("..adapter.in..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..adapter.out..")
            .check(classes);
    }
}
