package wiki.chiu.micro.auth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.adapter.in.http.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.api.AuthHttpService;

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
                    .importPackages("wiki.chiu.micro.auth"));
    }

    @Test
    void internalHttpHandlerImplementsThePublishedContract() {
        assertThat(AuthHttpService.class).isAssignableFrom(AuthInternalHttpHandler.class);
    }

    @Test
    void applicationServicesDependOnPortsInsteadOfRpcAdapters() {
        var classes =
            new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("wiki.chiu.micro.auth");
        noClasses()
            .that()
            .resideInAPackage("..service..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("wiki.chiu.micro.auth.adapter.out..")
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
                    .importPackages("wiki.chiu.micro.auth"));
    }

    @Test
    void portsAreFrameworkIndependentAndInputAdaptersUseInputPorts() {
        var classes =
            new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("wiki.chiu.micro.auth");
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
