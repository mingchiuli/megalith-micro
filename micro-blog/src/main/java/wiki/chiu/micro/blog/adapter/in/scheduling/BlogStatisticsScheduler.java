package wiki.chiu.micro.blog.adapter.in.scheduling;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.port.in.BlogStatisticsSync;
import wiki.chiu.micro.blog.config.BlogMaintenanceProperties;
import wiki.chiu.micro.scheduling.RedisTaskLock;

@Component
public class BlogStatisticsScheduler {

    private static final Logger log = LoggerFactory.getLogger(BlogStatisticsScheduler.class);
    private final BlogStatisticsSync statistics;
    private final RedisTaskLock lock;
    private final BlogMaintenanceProperties maintenance;
    private final boolean enabled;
    private final int batchSize;
    private final Counter failures;
    private final Counter processed;
    private final Timer duration;
    private final AtomicLong lastSuccess;

    public BlogStatisticsScheduler(
        BlogStatisticsSync statistics,
        RedisTaskLock lock,
        BlogMaintenanceProperties maintenance,
        @Value("${megalith.blog.statistics-sync.enabled:true}") boolean enabled,
        @Value("${megalith.blog.statistics-sync.batch-size:500}") int batchSize,
        MeterRegistry registry) {
        this.statistics = statistics;
        this.lock = lock;
        this.maintenance = maintenance;
        this.enabled = enabled;
        this.batchSize = batchSize;
        this.failures = registry.counter("megalith.blog.statistics.sync.failures");
        this.processed = registry.counter("megalith.blog.statistics.sync.documents");
        this.duration = registry.timer("megalith.blog.statistics.sync.duration");
        this.lastSuccess = registry.gauge("megalith.blog.statistics.sync.last.success", new AtomicLong());
    }

    @Scheduled(
        fixedDelayString = "${megalith.blog.statistics-sync.interval:60s}",
        initialDelayString = "${megalith.blog.statistics-sync.interval:60s}")
    public void synchronize() {
        if (!enabled || maintenance.isReadOnly()) {
            return;
        }
        try {
            lock.tryRun("blog:search-read-count-sync", () -> {
                long started = System.nanoTime();
                try {
                    processed.increment(statistics.synchronize(batchSize));
                    lastSuccess.set(Instant.now().getEpochSecond());
                } finally {
                    duration.record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
                }
            });
        } catch (RuntimeException failure) {
            failures.increment();
            log.error("Blog statistics synchronization failed; the next cycle will retry", failure);
        }
    }
}
