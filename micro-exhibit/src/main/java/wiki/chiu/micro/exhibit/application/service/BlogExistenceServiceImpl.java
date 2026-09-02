package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;

@Service
public class BlogExistenceServiceImpl implements BlogExistenceService {

    private static final Logger log = LoggerFactory.getLogger(BlogExistenceServiceImpl.class);

    private final BlogExistenceStore store;
    private final BlogCatalog catalog;
    private final int batchSize;
    private final Counter presentChecks;
    private final Counter absentChecks;
    private final Counter failOpenChecks;
    private final Counter rebuildSuccesses;
    private final Counter rebuildFailures;
    private final Timer rebuildDuration;

    public BlogExistenceServiceImpl(
        BlogExistenceStore store,
        BlogCatalog catalog,
        @Value("${megalith.blog.existence-index.batch-size:1000}") int batchSize,
        MeterRegistry meterRegistry) {
        this.store = store;
        this.catalog = catalog;
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException("existence index batch size must be between 1 and 1000");
        }
        this.batchSize = batchSize;
        this.presentChecks = checkCounter(meterRegistry, "present");
        this.absentChecks = checkCounter(meterRegistry, "absent");
        this.failOpenChecks = checkCounter(meterRegistry, "fail_open");
        this.rebuildSuccesses = rebuildCounter(meterRegistry, "success");
        this.rebuildFailures = rebuildCounter(meterRegistry, "failure");
        this.rebuildDuration =
            Timer.builder("megalith.blog.existence.rebuild.duration").register(meterRegistry);
    }

    @Override
    public void check(Long blogId) {
        BlogExistenceStore.State state = store.lookup(blogId);
        switch (state) {
            case PRESENT -> presentChecks.increment();
            case ABSENT -> {
                absentChecks.increment();
                throw new MissException(NO_FOUND.getMsg() + blogId + " blog");
            }
            case UNKNOWN -> failOpenChecks.increment();
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
            rebuildSuccesses.increment();
            log.info("Blog existence index rebuilt with {} entries", indexed);
        } catch (RuntimeException failure) {
            rebuildFailures.increment();
            log.error("Blog existence index rebuild failed", failure);
        } finally {
            if (attempted) {
                rebuildDuration.record(
                    System.nanoTime() - started, java.util.concurrent.TimeUnit.NANOSECONDS);
            }
        }
    }

    private static Counter checkCounter(MeterRegistry registry, String result) {
        return Counter.builder("megalith.blog.existence.checks")
            .tag("result", result)
            .register(registry);
    }

    private static Counter rebuildCounter(MeterRegistry registry, String result) {
        return Counter.builder("megalith.blog.existence.rebuilds")
            .tag("result", result)
            .register(registry);
    }
}
