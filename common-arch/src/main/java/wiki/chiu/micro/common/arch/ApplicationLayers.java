package wiki.chiu.micro.common.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        "wiki.chiu.micro.common.scheduling..",
        "wiki.chiu.micro.cache.."
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
        verify(rootPackage, Set.of());
    }

    /**
     * Verifies the shared application layout for a module that carries extra public packages beside
     * the layout, such as a Spring Boot starter whose published API lives in its own root packages.
     *
     * @param rootPackage the module's root package
     * @param extraLayerPackages additional package names allowed directly under the root
     */
    public static void verify(String rootPackage, Set<String> extraLayerPackages) {
        verify(productionClasses(rootPackage), rootPackage, extraLayerPackages);
    }

    /**
     * Verifies the shared application layout for one service against already imported classes.
     *
     * @param classes production classes of the service
     * @param rootPackage the service's root package, such as {@code wiki.chiu.micro.user}
     */
    public static void verify(JavaClasses classes, String rootPackage) {
        verify(classes, rootPackage, Set.of());
    }

    /**
     * Verifies the shared application layout for one module against already imported classes.
     *
     * @param classes production classes of the module
     * @param rootPackage the module's root package
     * @param extraLayerPackages additional package names allowed directly under the root
     */
    public static void verify(
        JavaClasses classes, String rootPackage, Set<String> extraLayerPackages) {
        Set<String> layers = allowedLayers(extraLayerPackages);
        String[] coreBanned = withoutSelf(CORE_BANNED, rootPackage);
        String[] applicationBanned = withoutSelf(APPLICATION_BANNED, rootPackage);
        assertNoCodeOutsideDeclaredLayers(classes, rootPackage, layers);
        assertDomainIsFrameworkFree(classes, rootPackage, coreBanned);
        assertApplicationIsFrameworkFree(classes, rootPackage, coreBanned, applicationBanned);
        assertApplicationDoesNotReachIntoAdaptersOrContracts(classes, rootPackage);
        assertAdaptersDoNotCrossDirections(classes, rootPackage);
        assertServicesOnlyDependOnImplementedInputPorts(classes, rootPackage);
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
    private static void assertNoCodeOutsideDeclaredLayers(
        JavaClasses classes, String rootPackage, Set<String> layers) {
        Set<String> offending = new LinkedHashSet<>();
        for (JavaClass javaClass : classes) {
            if (layerOf(javaClass.getPackageName(), rootPackage, layers) == null) {
                offending.add(javaClass.getName());
            }
        }
        if (!offending.isEmpty()) {
            throw new AssertionError(
                "Classes outside the declared layers of "
                    + rootPackage
                    + " (allowed: root, "
                    + layers
                    + "): "
                    + offending);
        }
    }

    /**
     * {@code domain} holds business state only: no framework, no infrastructure module, no other
     * service's wire contract, and no dependency on the layers that surround it.
     */
    private static void assertDomainIsFrameworkFree(
        JavaClasses classes, String rootPackage, String[] coreBanned) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "domain"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                merge(coreBanned, under(rootPackage, "application", "adapter", "config")))
            .allowEmptyShould(true)
            .check(classes);
    }

    /**
     * {@code application} is framework-free and only knows its own use cases and capabilities.
     */
    private static void assertApplicationIsFrameworkFree(
        JavaClasses classes,
        String rootPackage,
        String[] coreBanned,
        String[] applicationBanned) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "application"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                merge(
                    coreBanned,
                    applicationBanned,
                    under(rootPackage, "adapter", "config")))
            .allowEmptyShould(true)
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
            .allowEmptyShould(true)
            .check(classes);
    }

    /**
     * Input adapters call input ports only: they never reach into output ports, output adapters, or
     * application services, and no adapter reaches into the composition root.
     */
    private static void assertAdaptersDoNotCrossDirections(JavaClasses classes, String rootPackage) {
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter.in"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "adapter.out"))
            .allowEmptyShould(true)
            .check(classes);
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter.in"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "application.service"))
            .allowEmptyShould(true)
            .check(classes);
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter.in"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "application.port.out"))
            .allowEmptyShould(true)
            .check(classes);
        noClasses()
            .that()
            .resideInAnyPackage(under(rootPackage, "adapter"))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(under(rootPackage, "config"))
            .allowEmptyShould(true)
            .check(classes);
    }

    /**
     * A service may implement an input port, but it may not reach into a different use case's input
     * port: cross-use-case logic goes through output ports or a plain application collaborator.
     */
    private static void assertServicesOnlyDependOnImplementedInputPorts(
        JavaClasses classes, String rootPackage) {
        String portInPackage = rootPackage + ".application.port.in";
        classes()
            .that()
            .resideInAnyPackage(under(rootPackage, "application.service"))
            .should(
                new ArchCondition<JavaClass>("only depend on input ports they implement") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        Set<String> implemented =
                            item.getAllRawInterfaces().stream()
                                .map(JavaClass::getName)
                                .collect(Collectors.toSet());
                        item.getDirectDependenciesFromSelf().stream()
                            .map(Dependency::getTargetClass)
                            .filter(target -> target.getPackageName().equals(portInPackage))
                            .filter(target -> !implemented.contains(target.getName()))
                            .forEach(
                                target ->
                                    events.add(
                                        SimpleConditionEvent.violated(
                                            item,
                                            item.getName()
                                                + " depends on input port "
                                            + target.getName()
                                                + " which it does not implement")));
                    }
                })
            .allowEmptyShould(true)
            .check(classes);
    }

    private static Set<String> allowedLayers(Set<String> extraLayerPackages) {
        Set<String> layers = new LinkedHashSet<>(LAYER_PACKAGES);
        layers.addAll(extraLayerPackages);
        return layers;
    }

    /** Drops the module's own root from a ban list so shared modules can verify themselves. */
    private static String[] withoutSelf(String[] banned, String rootPackage) {
        String selfPattern = rootPackage + "..";
        return java.util.Arrays.stream(banned)
            .filter(entry -> !entry.equals(selfPattern) && !entry.equals(rootPackage))
            .toArray(String[]::new);
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

    private static String layerOf(
        String packageName, String rootPackage, Set<String> layers) {
        if (packageName.equals(rootPackage)) {
            return "";
        }
        if (!packageName.startsWith(rootPackage + ".")) {
            return null;
        }
        String remainder = packageName.substring(rootPackage.length() + 1);
        String first = remainder.split("\\.", 2)[0];
        return layers.contains(first) ? first : null;
    }
}
