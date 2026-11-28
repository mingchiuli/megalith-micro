package wiki.chiu.micro.user.adapter.in.scheduling;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.scheduling.RedisTaskLock;
import wiki.chiu.micro.user.application.port.in.UserIdentityService;
import org.springframework.beans.factory.annotation.Value;

@Component
public class PasswordUnlockScheduler {

    private static final Logger log = LoggerFactory.getLogger(PasswordUnlockScheduler.class);
    private static final String TASK_NAME = "user:password-unlock";

    private final RedisTaskLock taskLock;
    private final UserIdentityService identities;
    private final int maxBatchesPerRun;
    private final int batchSize;
    private final Counter lockMisses;
    private final Counter failures;

    public PasswordUnlockScheduler(
        RedisTaskLock taskLock,
        UserIdentityService identities,
        @Value("${megalith.user.password-lock.max-batches-per-run:10}") int maxBatchesPerRun,
        @Value("${megalith.user.password-lock.batch-size:100}") int batchSize,
        MeterRegistry meterRegistry) {
        this.taskLock = taskLock;
        this.identities = identities;
        this.maxBatchesPerRun = maxBatchesPerRun;
        this.batchSize = batchSize;
        this.lockMisses = meterRegistry.counter("megalith.user.password.unlock.lock.misses");
        this.failures = meterRegistry.counter("megalith.user.password.unlock.failures");
    }

    @Scheduled(fixedDelayString = "${megalith.user.password-lock.poll-interval:30s}")
    public void unlockExpired() {
        try {
            if (!taskLock.tryRun(TASK_NAME, this::drainExpiredLocks)) {
                lockMisses.increment();
            }
        } catch (RuntimeException failure) {
            failures.increment();
            log.error("Password unlock cycle failed", failure);
        }
    }

    private void drainExpiredLocks() {
        for (int batch = 0; batch < maxBatchesPerRun; batch++) {
            int unlocked = identities.unlockExpiredBatch();
            if (unlocked < batchSize) {
                return;
            }
        }
    }
}
