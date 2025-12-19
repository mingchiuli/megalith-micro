package wiki.chiu.micro.blog.config;

import org.hibernate.annotations.EmbeddedTable;
import org.hibernate.boot.models.annotations.internal.EmbeddedTableAnnotation;
import org.hibernate.event.spi.PreFlushEventListener;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.Set;

import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS;

public class Hibernate72RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {

        // loggers
        hints.reflection()
                .registerTypes(Set.of(TypeReference.of("org.hibernate.action.internal.ActionLogging_$logger"),
                                TypeReference.of("org.hibernate.boot.BootLogging_$logger"),
                                TypeReference.of("org.hibernate.boot.beanvalidation.BeanValidationLogger_$logger"),
                                TypeReference.of("org.hibernate.bytecode.enhance.spi.interceptor.BytecodeInterceptorLogging_$logger"),
                                TypeReference.of("org.hibernate.collection.internal.CollectionLogger_$logger"),
                                TypeReference.of("org.hibernate.context.internal.CurrentSessionLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.internal.NaturalIdLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.internal.PersistenceContextLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.internal.SessionMetricsLogger_$logger"),
                                TypeReference.of("org.hibernate.engine.jdbc.JdbcLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.jdbc.batch.JdbcBatchLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.jdbc.connections.internal.ConnectionProviderLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.jdbc.env.internal.LobCreationLogging_$logger"),
                                TypeReference.of("org.hibernate.engine.jdbc.spi.SQLExceptionLogging_$logger"),
                                TypeReference.of("org.hibernate.event.internal.EntityCopyLogging_$logger"),
                                TypeReference.of("org.hibernate.event.internal.EventListenerLogging_$logger"),
                                TypeReference.of("org.hibernate.id.UUIDLogger_$logger"),
                                TypeReference.of("org.hibernate.id.enhanced.OptimizerLogger_$logger"),
                                TypeReference.of("org.hibernate.internal.SessionFactoryLogging_$logger"),
                                TypeReference.of("org.hibernate.internal.SessionLogging_$logger"),
                                TypeReference.of("org.hibernate.internal.log.StatisticsLogger_$logger"),
                                TypeReference.of("org.hibernate.jpa.internal.JpaLogger_$logger"),
                                TypeReference.of("org.hibernate.loader.ast.internal.MultiKeyLoadLogging_$logger"),
                                TypeReference.of("org.hibernate.metamodel.mapping.MappingModelCreationLogging_$logger"),
                                TypeReference.of("org.hibernate.query.QueryLogging_$logger"),
                                TypeReference.of("org.hibernate.query.hql.HqlLogging_$logger"),
                                TypeReference.of("org.hibernate.resource.jdbc.internal.LogicalConnectionLogging_$logger"),
                                TypeReference.of("org.hibernate.resource.transaction.backend.jta.internal.JtaLogging_$logger"),
                                TypeReference.of("org.hibernate.resource.transaction.internal.SynchronizationLogging_$logger"),
                                TypeReference.of("org.hibernate.service.internal.ServiceLogger_$logger"),
                                TypeReference.of("org.hibernate.sql.model.ModelMutationLogging_$logger"),
                                TypeReference.of("org.hibernate.sql.results.graph.embeddable.EmbeddableLoadingLogger_$logger")),
                        hint -> hint.withMembers(INVOKE_DECLARED_CONSTRUCTORS, INVOKE_PUBLIC_METHODS));

        // events
        hints.reflection().registerType(PreFlushEventListener.class, INVOKE_DECLARED_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(PreFlushEventListener[].class, INVOKE_DECLARED_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);

        // annotations
        hints.reflection().registerType(EmbeddedTable.class, INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(EmbeddedTableAnnotation.class, INVOKE_DECLARED_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
    }

}