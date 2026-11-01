package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.util.Map;
import java.util.Set;

import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

/**
 * Initializes repository-backed aliases without turning the alias name into a concrete index.
 */
public class AliasInitializingElasticsearchRepository<T, ID>
    extends SimpleElasticsearchRepository<T, ID> {

    private static final String BOOTSTRAP_SUFFIX = "-v1-bootstrap";

    public AliasInitializingElasticsearchRepository(
        ElasticsearchEntityInformation<T, ID> metadata,
        ElasticsearchOperations operations) {
        super(metadata, operations);
    }

    @Override
    public void createIndexAndMappingIfNeeded() {
        if ("true".equals(System.getenv("SPRING_DATA_ELASTICSEARCH_SKIP_REPOSITORY_INIT"))) {
            return;
        }

        String alias = entityInformation.getIndexCoordinates().getIndexName();
        if (aliasOperations().exists()) {
            requireAlias(alias);
            return;
        }

        String physicalIndex = alias + BOOTSTRAP_SUFFIX;
        IndexOperations physical = operations.indexOps(IndexCoordinates.of(physicalIndex));
        if (!physical.exists()) {
            try {
                boolean created =
                    physical.create(
                        operations.indexOps(entityClass).createSettings(entityClass),
                        operations.indexOps(entityClass).createMapping(entityClass));
                if (!created) {
                    throw new IllegalStateException(
                        "search bootstrap index was not created: " + physicalIndex);
                }
            } catch (RuntimeException failure) {
                if (!physical.exists()) {
                    throw failure;
                }
            }
        }

        boolean acknowledged =
            aliasOperations()
                .alias(
                    new AliasActions(
                        new AliasAction.Add(
                            AliasActionParameters.builder()
                                .withIndices(physicalIndex)
                                .withAliases(alias)
                                .withIsWriteIndex(true)
                                .build())));
        if (!acknowledged) {
            throw new IllegalStateException("search alias creation was not acknowledged: " + alias);
        }
        requireAlias(alias);
    }

    private IndexOperations aliasOperations() {
        return operations.indexOps(entityClass);
    }

    private void requireAlias(String alias) {
        try {
            Map<String, Set<AliasData>> aliases = aliasOperations().getAliases(alias);
            if (aliases.isEmpty()
                || aliases.values().stream().flatMap(Set::stream).noneMatch(this::isWriteAlias)) {
                throw new IllegalStateException(
                    "search name must be an alias with a write index: " + alias);
            }
        } catch (ResourceNotFoundException failure) {
            throw new IllegalStateException(
                "search name is occupied by a concrete index: " + alias, failure);
        }
    }

    private boolean isWriteAlias(AliasData alias) {
        return Boolean.TRUE.equals(alias.isWriteIndex());
    }
}
