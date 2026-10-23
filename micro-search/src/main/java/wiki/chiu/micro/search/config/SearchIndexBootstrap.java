package wiki.chiu.micro.search.config;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.scheduling.RedisTaskLock;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchIndexMaintenanceAdapter;

/**
 * Initializes public and private aliases before Rabbit listeners and schedulers start.
 */
@Component
public class SearchIndexBootstrap implements SmartInitializingSingleton {

    private final ElasticsearchIndexMaintenanceAdapter publicIndexes;
    private final AdminSearchIndexInitializer adminIndexes;
    private final RedisTaskLock lock;

    public SearchIndexBootstrap(
        ElasticsearchIndexMaintenanceAdapter publicIndexes,
        AdminSearchIndexInitializer adminIndexes,
        RedisTaskLock lock) {
        this.publicIndexes = publicIndexes;
        this.adminIndexes = adminIndexes;
        this.lock = lock;
    }

    @Override
    public void afterSingletonsInstantiated() {
        lock.run(
            "search:index-bootstrap",
            () -> {
                publicIndexes.ensureAlias();
                publicIndexes.repairLegacyDocuments();
                try {
                    adminIndexes.ensureAliases();
                } catch (IOException failure) {
                    throw new UncheckedIOException(failure);
                }
                return null;
            });
    }
}
