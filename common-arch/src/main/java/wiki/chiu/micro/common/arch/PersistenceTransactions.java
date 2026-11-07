package wiki.chiu.micro.common.arch;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import com.tngtech.archunit.core.domain.JavaMethod;

/** Checks repository reads reachable from transactional persistence writers, regardless of class name. */
final class PersistenceTransactions {

    private static final String TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";
    private static final String REPOSITORY = "org.springframework.data.repository.Repository";
    private static final String QUERY = "org.springframework.data.jpa.repository.Query";
    private static final String MODIFYING = "org.springframework.data.jpa.repository.Modifying";
    private static final Pattern READ_METHOD = Pattern.compile("^(find|count|exists|get|read|query|load|search|stream).*");

    private PersistenceTransactions() {
    }

    static void verify(JavaClasses classes, String rootPackage) {
        Set<String> violations = new TreeSet<>();
        for (JavaClass type : classes) {
            if (!isPersistenceAdapter(type, rootPackage)) {
                continue;
            }
            for (JavaMethod method : type.getAllMethods()) {
                if (isPersistenceType(method.getOwner(), rootPackage) && isWriteTransaction(type, method)) {
                    findReads(method, method, rootPackage, new HashSet<>(), violations);
                }
            }
        }
        if (!violations.isEmpty()) {
            throw new AssertionError("Transactional persistence writers must not read repositories:\n"
                + String.join("\n", violations));
        }
    }

    private static boolean isPersistenceAdapter(JavaClass type, String rootPackage) {
        return isPersistenceType(type, rootPackage) && !type.isInterface() && !type.isAssignableTo(REPOSITORY);
    }

    private static boolean isPersistenceType(JavaClass type, String rootPackage) {
        String persistence = rootPackage + ".adapter.out.persistence";
        String name = type.getPackageName();
        return name.equals(persistence) || name.startsWith(persistence + ".");
    }

    private static boolean isWriteTransaction(JavaClass type, JavaMethod method) {
        Optional<Boolean> readOnly = method.tryGetAnnotationOfType(TRANSACTIONAL)
            .map(annotation -> Boolean.TRUE.equals(annotation.getProperties().get("readOnly")));
        if (readOnly.isEmpty()) {
            readOnly = classTransaction(type);
        }
        return readOnly.isPresent() && !readOnly.get();
    }

    private static Optional<Boolean> classTransaction(JavaClass type) {
        Optional<Boolean> readOnly = type.tryGetAnnotationOfType(TRANSACTIONAL)
            .map(annotation -> Boolean.TRUE.equals(annotation.getProperties().get("readOnly")));
        return readOnly.isPresent() ? readOnly : type.getRawSuperclass().flatMap(PersistenceTransactions::classTransaction);
    }

    private static void findReads(
        JavaCodeUnit entry,
        JavaCodeUnit current,
        String rootPackage,
        Set<String> visited,
        Set<String> violations) {
        if (!visited.add(current.getFullName())) {
            return;
        }
        for (var access : current.getAccessesFromSelf()) {
            if (!(access instanceof JavaCodeUnitAccess<?> call)) {
                continue;
            }
            var target = call.getTarget();
            var member = target.resolveMember();
            if (call.getTargetOwner().isAssignableTo(REPOSITORY)) {
                boolean modifying = member.map(value -> value.isAnnotatedWith(MODIFYING)).orElse(false);
                boolean query = member.map(value -> value.isAnnotatedWith(QUERY)).orElse(false);
                if (!modifying && (query || READ_METHOD.matcher(call.getName()).matches())) {
                    violations.add(entry.getFullName() + " reaches " + call.getDescription());
                }
            }
            if (isPersistenceType(call.getTargetOwner(), rootPackage)) {
                member.ifPresent(helper -> findReads(entry, helper, rootPackage, visited, violations));
            }
        }
    }
}
