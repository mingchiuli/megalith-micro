package wiki.chiu.micro.auth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.auth.handler.AuthInternalHttpHandler;

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
        .resideInAPackage("wiki.chiu.micro.auth.rpc..")
        .check(classes);
    noClasses()
        .that()
        .resideInAPackage("..service..")
        .should()
        .dependOnClassesThat()
        .haveSimpleNameEndingWith("HttpService")
        .check(classes);
  }
}
