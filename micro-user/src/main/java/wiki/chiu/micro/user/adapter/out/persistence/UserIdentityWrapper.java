package wiki.chiu.micro.user.adapter.out.persistence;

import static wiki.chiu.micro.common.enums.StatusEnum.HIDE;
import static wiki.chiu.micro.common.enums.StatusEnum.NORMAL;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRepository;
import wiki.chiu.micro.user.application.port.out.UserIdentityWriter;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;

@Component
public class UserIdentityWrapper implements UserIdentityWriter {

    private final UserRepository users;
    private final AuthCacheEvictionOutbox cacheEvictions;
    private final Duration passwordLockDuration;
    private final Counter locked;
    private final Counter unlocked;

    public UserIdentityWrapper(
        UserRepository users,
        AuthCacheEvictionOutbox cacheEvictions,
        @Value("${megalith.user.password-lock.duration:15m}") Duration passwordLockDuration,
        MeterRegistry meterRegistry) {
        this.users = users;
        this.cacheEvictions = cacheEvictions;
        this.passwordLockDuration = passwordLockDuration;
        this.locked = meterRegistry.counter("megalith.user.password.locked");
        this.unlocked = meterRegistry.counter("megalith.user.password.unlocked");
    }

    @Transactional
    @Override
    public void updateLoginTime(String login, LocalDateTime time) {
        users.updateLoginTime(login, time);
    }

    @Transactional
    @Override
    public void lockAfterPasswordFailures(Long userId) {
        int updated =
            users.lockAfterPasswordFailures(
                userId, NORMAL.getCode(), HIDE.getCode(), passwordLockDuration.toSeconds());
        if (updated == 1) {
            cacheEvictions.enqueue(List.of(userId), List.of(), List.of(), false, false);
            locked.increment();
        }
    }

    @Transactional
    @Override
    public int unlockExpired(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return 0;
        }
        int updated = users.unlockExpiredPasswordLocks(userIds, HIDE.getCode(), NORMAL.getCode());
        if (updated > 0) {
            cacheEvictions.enqueue(userIds, List.of(), List.of(), false, false);
            unlocked.increment(updated);
        }
        return updated;
    }
}
