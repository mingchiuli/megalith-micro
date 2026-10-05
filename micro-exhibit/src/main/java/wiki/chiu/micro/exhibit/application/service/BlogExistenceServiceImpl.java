package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.ExistenceIndexMetrics;

public class BlogExistenceServiceImpl implements BlogExistenceService {

    private static final Logger log = LoggerFactory.getLogger(BlogExistenceServiceImpl.class);

    private static final int MAX_BATCH_SIZE = 1000;

    private final BlogExistenceStore store;
    private final BlogCatalog catalog;
    private final int batchSize;
    private final ExistenceIndexMetrics metrics;

    public BlogExistenceServiceImpl(
        BlogExistenceStore store,
        BlogCatalog catalog,
        int batchSize,
        ExistenceIndexMetrics metrics) {
        if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException(
                "existence index batch size must be between 1 and " + MAX_BATCH_SIZE);
        }
        this.store = store;
        this.catalog = catalog;
        this.batchSize = batchSize;
        this.metrics = metrics;
    }

    @Override
    public void check(Long blogId) {
        BlogExistenceStore.State state = store.lookup(blogId);
        switch (state) {
            case PRESENT -> metrics.presentCheck();
            case ABSENT -> {
                metrics.absentCheck();
                throw new MissException(NO_FOUND.getMsg() + blogId + " blog");
            }
            case UNKNOWN -> metrics.failOpenCheck();
        }
    }

    @Override
    public void markPresent(Long blogId) {
        store.markPresent(blogId);
    }

    @Override
    public void markAbsent(Long blogId) {
        store.markAbsent(blogId);
    }

    @Override
    public void rebuildIfRequired() {
        long started = System.nanoTime();
        boolean attempted = false;
        try {
            var rebuild = store.tryBeginRebuild();
            if (rebuild.isEmpty()) {
                return;
            }
            attempted = true;
            long indexed = 0;
            try (BlogExistenceStore.Rebuild session = rebuild.orElseThrow()) {
                long afterId = 0;
                while (true) {
                    List<Long> ids = catalog.findIdsAfter(afterId, batchSize);
                    if (ids.isEmpty()) {
                        break;
                    }
                    session.addAll(ids);
                    indexed += ids.size();
                    afterId = ids.getLast();
                    if (ids.size() < batchSize) {
                        break;
                    }
                }
                session.publish();
            }
            metrics.rebuildSucceeded(indexed);
            log.info("Blog existence index rebuilt with {} entries", indexed);
        } catch (RuntimeException failure) {
            metrics.rebuildFailed();
            log.error("Blog existence index rebuild failed", failure);
        } finally {
            if (attempted) {
                metrics.rebuildDurationNanos(System.nanoTime() - started);
            }
        }
    }
}
