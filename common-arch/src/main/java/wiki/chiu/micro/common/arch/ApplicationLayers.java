package wiki.chiu.micro.common.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Executable form of the ports-and-adapters layout every Java application in this repository
 * shares. The rules are defined once so that the same package name means the same thing in every
 * service; a service declares only the module-specific rules it adds on top.
 *
 * <p>The layout is {@code <root>.domain}, {@code <root>.application.{model,port.in,port.out,
 * service}}, {@code <root>.adapter.in.*}, {@code <root>.adapter.out.*}, and {@code <root>.config}.
 * {@code domain} and {@code application} are framework-free: they may only depend on the JDK and on
 * framework-free shared contracts. Adapters may use Spring but never reach into the composition
 * root or into the opposite direction. {@code config} wires the graph and is depended on by no one.
 */
public final class ApplicationLayers {

    /** Framework and infrastructure packages that the core layers must not touch. */
    private static final String[] CORE_BANNED = {
        "org.springframework..",
        "org.hibernate..",
        "org.redisson..",
        "jakarta..",
        "tools.jackson..",
        "com.fasterxml.jackson..",
        "io.micrometer..",
        "co.elastic.clients..",
        "wiki.chiu.micro.common.rpc..",
        "wiki.chiu.micro.common.web..",
        "wiki.chiu.micro.common.auth.web..",
        "wiki.chiu.micro.common.observability..",
        "wiki.chiu.micro.common.messaging..",
        "wiki.chiu.micro.common.outbox..",
        "wiki.chiu.micro.common.scheduling.."
    };

    /** Shared modules that the application layer must not reach into from its own code. */
    private static final String[] APPLICATION_BANNED = {
        "wiki.chiu.micro.cache..", "wiki.chiu.micro.*.api.."
    };

    /**
     * Package names allowed directly under an application root package. {@code api} is not an
     * application layer: it is the published contract module {@code api-<service>} that shares the
     * application's namespace and is on its compile classpath.
     */
    private static final Set<String> LAYER_PACKAGES =
        Set.of("domain", "application", "adapter", "config", "api");

    private ApplicationLayers() {
    }

    /**
     * Verifies the shared application layout for one service.
     *
     * @param rootPackage the service's root package, such as {@code wiki.chiu.micro.user}
     */
    public static void verify(String rootPackage) {
        verify(productionClasses(rootPackage), rootPackage);
    }

    /**
     * Verifies the shared application layout for one service against already imported classes.
     *
     * @param classes production classes of the service
     * @param rootPackage the service's root package, such as {@code wiki.chiu.micro.user}
     */
    public static void verify(JavaClasses classes, String rootPackage) {
        assertNoCodeOutsideDeclaredLayers(classes, rootPackage);
        assertDomainIsFrameworkFree(classes, rootPackage);
        assertApplicationIsFrameworkFree(classes, rootPackage);
        assertApplicationDoesNotReachIntoAdaptersOrContracts(classes, rootPackage);
        assertAdaptersDoNotCrossDirections(classes, rootPackage);
    }

    /**
     * Imports the production classes of a service.
     *
     * @param rootPackage the service's root package
     * @return the imported production classes
     */
    public static JavaClasses productionClasses(String rootPackage) {
        return new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(rootPackage);
    }

    /**
     * Every class belongs to one of the declared layer packages, so utility or transport buckets
     * cannot appear beside the layout.
     */
    private static void assertNoCodeOutsideDeclaredLayers(JavaClasses classes, String rootPackage) {
        Set<String> offending = new LinkedHashSet<>();
        for (JavaClass javaClass : classes) {
            if (layerOf(javaClass.getPackageName(), rootPackage) == null) {
                offending.add(javaClass.getName());
            }
        }
        if (!offending.isEmpty()) {
            throw new AssertionError(
                "Classes outside the declared layers of "
                    + rootPackage
                    + " (allowed: root, "
                    + LAYER_PACKAGES
                    + "): "
                    + offending);
        }
    }

    /**
     * {@code domain} holds business state only: no framework, no infrastructure module, no other
     * service's wire contract, and no dependency on the layers that surround it.
     */
    private static void assertDomainIsFrameworkFree(JavaClasses classes, String rootPackage) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "domain"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(merge(CORE_BANNED, under(rootPackage, "application", "adapter", "config")))
            .check(classes);
    }

    /**
     * {@code application} is framework-free and only knows its own use cases and capabilities.
     */
    private static void assertApplicationIsFrameworkFree(JavaClasses classes, String rootPackage) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "application"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                merge(
                    CORE_BANNED,
                    APPLICATION_BANNED,
                    under(rootPackage, "adapter", "config")))
            .check(classes);
    }

    /**
     * The application layer never names HTTP handlers, routes, or repositories; those live behind
     * the ports it declares.
     */
    private static void assertApplicationDoesNotReachIntoAdaptersOrContracts(
        JavaClasses classes, String rootPackage) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "application"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..handler..", "..route..", "..routes..", "..repository..")
            .check(classes);
    }

    /**
     * Input adapters call input ports rather than output adapters or application services, and no
     * adapter reaches into the composition root.
     */
    private static void assertAdaptersDoNotCrossDirections(JavaClasses classes, String rootPackage) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter.in"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "adapter.out"))
            .check(classes);
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter.in"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "application.service"))
            .check(classes);
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "config"))
            .check(classes);
    }

    /**
     * Matches a layer package and everything beneath it, because ArchUnit's package patterns match
     * the named package only unless the pattern ends in {@code ..}.
     */
    private static String[] under(String rootPackage, String... layerPackages) {
        return java.util.Arrays.stream(layerPackages)
            .flatMap(
                layer -> {
                    String base = rootPackage + "." + layer;
                    return java.util.stream.Stream.of(base, base + "..");
                })
            .toArray(String[]::new);
    }

    private static String[] merge(String[]... groups) {
        Set<String> all = new LinkedHashSet<>();
        for (String[] group : groups) {
            all.addAll(List.of(group));
        }
        return all.toArray(String[]::new);
    }

    private static String layerOf(String packageName, String rootPackage) {
        if (packageName.equals(rootPackage)) {
            return "";
        }
        if (!packageName.startsWith(rootPackage + ".")) {
            return null;
        }
        String remainder = packageName.substring(rootPackage.length() + 1);
        String first = remainder.split("\\.", 2)[0];
        return LAYER_PACKAGES.contains(first) ? first : null;
    }
}
