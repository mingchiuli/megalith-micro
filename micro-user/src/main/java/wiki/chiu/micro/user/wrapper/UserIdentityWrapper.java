package wiki.chiu.micro.user.wrapper;

import static wiki.chiu.micro.common.lang.StatusEnum.HIDE;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.config.PasswordLockProperties;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class UserIdentityWrapper {

  private final UserRepository users;
  private final AuthCacheEvictionOutbox cacheEvictions;
  private final PasswordLockProperties properties;
  private final Counter locked;
  private final Counter unlocked;

  public UserIdentityWrapper(
      UserRepository users,
      AuthCacheEvictionOutbox cacheEvictions,
      PasswordLockProperties properties,
      MeterRegistry meterRegistry) {
    this.users = users;
    this.cacheEvictions = cacheEvictions;
    this.properties = properties;
    this.locked = meterRegistry.counter("megalith.user.password.locked");
    this.unlocked = meterRegistry.counter("megalith.user.password.unlocked");
  }

  @Transactional
  public void updateLoginTime(String login, LocalDateTime time) {
    users.updateLoginTime(login, time);
  }

  @Transactional
  public void lockAfterPasswordFailures(Long userId) {
    int updated =
        users.lockAfterPasswordFailures(
            userId, NORMAL.getCode(), HIDE.getCode(), properties.getDuration().toSeconds());
    if (updated == 1) {
      cacheEvictions.enqueue(List.of(userId), List.of(), List.of(), false, false);
      locked.increment();
    }
  }

  @Transactional
  public int unlockExpiredBatch() {
    List<UserEntity> expired =
        users.findExpiredPasswordLocks(
            HIDE.getCode(), PageRequest.of(0, properties.getBatchSize()));
    if (expired.isEmpty()) {
      return 0;
    }
    expired.forEach(
        user -> {
          user.setStatus(NORMAL.getCode());
          user.setPasswordLockedUntil(null);
        });
    users.saveAll(expired);
    cacheEvictions.enqueue(
        expired.stream().map(UserEntity::getId).toList(), List.of(), List.of(), false, false);
    unlocked.increment(expired.size());
    return expired.size();
  }
}
