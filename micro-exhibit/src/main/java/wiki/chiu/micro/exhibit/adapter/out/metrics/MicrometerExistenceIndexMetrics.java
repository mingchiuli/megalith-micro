package wiki.chiu.micro.exhibit.adapter.out.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.out.ExistenceIndexMetrics;

@Component
public class MicrometerExistenceIndexMetrics implements ExistenceIndexMetrics {

    private static final String CHECKS = "megalith.blog.existence.checks";
    private static final String REBUILDS = "megalith.blog.existence.rebuilds";
    private static final String REBUILD_DURATION = "megalith.blog.existence.rebuild.duration";
    private static final String RESULT = "result";

    private final Counter presentChecks;
    private final Counter absentChecks;
    private final Counter failOpenChecks;
    private final Counter rebuildSuccesses;
    private final Counter rebuildFailures;
    private final Timer rebuildDuration;

    public MicrometerExistenceIndexMetrics(MeterRegistry meterRegistry) {
        this.presentChecks = counter(meterRegistry, "present");
        this.absentChecks = counter(meterRegistry, "absent");
        this.failOpenChecks = counter(meterRegistry, "fail_open");
        this.rebuildSuccesses = rebuildCounter(meterRegistry, "success");
        this.rebuildFailures = rebuildCounter(meterRegistry, "failure");
        this.rebuildDuration = Timer.builder(REBUILD_DURATION).register(meterRegistry);
    }

    @Override
    public void presentCheck() {
        presentChecks.increment();
    }

    @Override
    public void absentCheck() {
        absentChecks.increment();
    }

    @Override
    public void failOpenCheck() {
        failOpenChecks.increment();
    }

    @Override
    public void rebuildSucceeded(long indexed) {
        rebuildSuccesses.increment();
    }

    @Override
    public void rebuildFailed() {
        rebuildFailures.increment();
    }

    @Override
    public void rebuildDurationNanos(long nanos) {
        rebuildDuration.record(nanos, TimeUnit.NANOSECONDS);
    }

    private static Counter counter(MeterRegistry registry, String result) {
        return Counter.builder(CHECKS).tag(RESULT, result).register(registry);
    }

    private static Counter rebuildCounter(MeterRegistry registry, String result) {
        return Counter.builder(REBUILDS).tag(RESULT, result).register(registry);
    }
}
