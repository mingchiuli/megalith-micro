package wiki.chiu.micro.blog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
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
        .check(new ClassFileImporter().importPackages("wiki.chiu.micro.blog"));
  }
}
