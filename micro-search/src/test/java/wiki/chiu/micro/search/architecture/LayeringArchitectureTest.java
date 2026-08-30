package wiki.chiu.micro.search.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class LayeringArchitectureTest {

  private static final String ROOT = "wiki.chiu.micro.search";

  @Test
  void domainAndApplicationAreFrameworkIndependent() {
    var classes = productionClasses();
    noClasses()
        .that()
        .resideInAnyPackage("..domain..", "..application..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "co.elastic.clients..",
            "tools.jackson..",
            "com.fasterxml.jackson..")
        .check(classes);
  }

  @Test
  void applicationDoesNotDependOnAdaptersOrHttpContracts() {
    var classes = productionClasses();
    noClasses()
        .that()
        .resideInAPackage("..application..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..adapter..", "..repository..", "wiki.chiu.micro.search.api..")
        .check(classes);
  }

  @Test
  void inputAdaptersDoNotCallOutputAdaptersDirectly() {
    noClasses()
        .that()
        .resideInAPackage("..adapter.in..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..adapter.out..")
        .check(productionClasses());
  }

  @Test
  void outputPortsAreOwnedByTheApplication() {
    noClasses()
        .that()
        .resideInAPackage("..application.port.out..")
        .should()
        .dependOnClassesThat()
        .resideOutsideOfPackages("..domain..", "..application..", "java..")
        .check(productionClasses());
  }

  private static com.tngtech.archunit.core.domain.JavaClasses productionClasses() {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages(ROOT);
  }
}
