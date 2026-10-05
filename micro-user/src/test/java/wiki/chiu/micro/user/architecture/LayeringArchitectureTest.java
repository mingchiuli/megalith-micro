package wiki.chiu.micro.user.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import wiki.chiu.micro.common.arch.ApplicationLayers;

class LayeringArchitectureTest {

    private static final String ROOT = "wiki.chiu.micro.user";

    private static final Pattern JOIN_KEYWORD =
        Pattern.compile("\\bjoin\\b", Pattern.CASE_INSENSITIVE);

    @Test
    void applicationLayersAreRespected() {
        ApplicationLayers.verify(ROOT);
    }

    @Test
    void repositoryQueriesDoNotUseJoins() {
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(ROOT + ".adapter.out.persistence.repository")
            .stream()
            .flatMap(repository -> repository.getMethods().stream())
            .filter(method -> method.isAnnotatedWith(Query.class))
            .forEach(
                method -> {
                    Query query = method.getAnnotationOfType(Query.class);
                    assertNoJoin(method.getFullName(), query.value());
                    assertNoJoin(method.getFullName(), query.countQuery());
                });
    }

    @Test
    void transactionalWrappersDoNotCallRepositoryReadMethods() {
        var readCalls =
            new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT)
                .stream()
                .filter(type -> type.getSimpleName().endsWith("Wrapper"))
                .flatMap(type -> type.getMethodCallsFromSelf().stream())
                .filter(call -> call.getTargetOwner().getPackageName().contains(".repository"))
                .filter(
                    call ->
                        call.getName()
                            .matches("^(find|count|exists|get|read|query|load|search).*$"))
                .map(Object::toString)
                .sorted()
                .toList();

        assertTrue(readCalls.isEmpty(), () -> "wrapper repository reads: " + readCalls);
    }

    @Test
    void wrappersDoNotDelegateToApplicationServices() {
        noClasses()
            .that()
            .resideInAPackage("..adapter.out.persistence..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..application.service..")
            .check(ApplicationLayers.productionClasses(ROOT));
    }

    private void assertNoJoin(String method, String query) {
        assertFalse(
            JOIN_KEYWORD.matcher(query).find(), () -> method + " must compose related data in Java");
    }
}
