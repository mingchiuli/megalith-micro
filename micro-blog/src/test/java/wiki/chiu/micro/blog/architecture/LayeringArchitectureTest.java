package wiki.chiu.micro.blog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class LayeringArchitectureTest {

  @Test
  void applicationServicesDoNotDependOnWebOrDeliveryLayers() {
    noClasses()
        .that()
        .resideInAPackage("..service..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.servlet..", "org.springframework.web..", "..handler..", "..route..")
        .check(
            new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("wiki.chiu.micro.blog"));
  }

  @Test
  void applicationServicesDependOnPortsInsteadOfRpcAdapters() {
    var classes =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("wiki.chiu.micro.blog");
    noClasses()
        .that()
        .resideInAPackage("..service..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("wiki.chiu.micro.blog.rpc..")
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
