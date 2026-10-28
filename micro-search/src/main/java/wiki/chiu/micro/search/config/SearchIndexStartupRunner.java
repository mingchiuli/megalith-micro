package wiki.chiu.micro.search.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.scheduling.RedisTaskLock;
import wiki.chiu.micro.search.application.port.in.BootstrapSearchIndexes;

/**
 * Creates all search aliases and backfills their database projections before listeners start.
 */
@Component
public class SearchIndexStartupRunner implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexStartupRunner.class);

    private final BootstrapSearchIndexes bootstrap;
    private final RedisTaskLock lock;
    private final boolean enabled;
    private final int batchSize;

    public SearchIndexStartupRunner(
        BootstrapSearchIndexes bootstrap,
        RedisTaskLock lock,
        @Value("${megalith.search.bootstrap.enabled:true}") boolean enabled,
        @Value("${megalith.search.bootstrap.batch-size:500}") int batchSize) {
        this.bootstrap = bootstrap;
        this.lock = lock;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!enabled) {
            log.info("Search index bootstrap is disabled");
            return;
        }
        lock.run("search:index-bootstrap", () -> {
            bootstrap.bootstrap(batchSize);
            log.info("Search index bootstrap completed");
            return null;
        });
    }
}
