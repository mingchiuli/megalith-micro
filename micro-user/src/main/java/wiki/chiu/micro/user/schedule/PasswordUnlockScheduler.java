package wiki.chiu.micro.user.schedule;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.scheduling.RedisTaskLock;
import wiki.chiu.micro.user.application.port.in.UserIdentityService;
import wiki.chiu.micro.user.config.PasswordLockProperties;

@Component
public class PasswordUnlockScheduler {

    private static final Logger log = LoggerFactory.getLogger(PasswordUnlockScheduler.class);
    private static final String TASK_NAME = "user:password-unlock";

    private final RedisTaskLock taskLock;
    private final UserIdentityService identities;
    private final PasswordLockProperties properties;
    private final Counter lockMisses;
    private final Counter failures;

    public PasswordUnlockScheduler(
        RedisTaskLock taskLock,
        UserIdentityService identities,
        PasswordLockProperties properties,
        MeterRegistry meterRegistry) {
        this.taskLock = taskLock;
        this.identities = identities;
        this.properties = properties;
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
        for (int batch = 0; batch < properties.getMaxBatchesPerRun(); batch++) {
            int unlocked = identities.unlockExpiredBatch();
            if (unlocked < properties.getBatchSize()) {
                return;
            }
        }
    }
}
