package wiki.chiu.micro.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import wiki.chiu.micro.user.entity.UserEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:53 am
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByUsernameOrEmailOrPhone(String username, String email, String phone);

  @Query(
      value =
          "UPDATE UserEntity user set user.lastLogin = ?2 where (user.username = ?1 or user.email = ?1 or user.phone = ?1)")
  @Modifying
  void updateLoginTime(String username, LocalDateTime time);

  @Query(
      value =
          """
          UPDATE m_user
          SET status = :lockedStatus,
              password_locked_until = TIMESTAMPADD(SECOND, :lockSeconds, CURRENT_TIMESTAMP(6))
          WHERE id = :userId
            AND status = :normalStatus
            AND password_locked_until IS NULL
          """,
      nativeQuery = true)
  @Modifying
  int lockAfterPasswordFailures(Long userId, int normalStatus, int lockedStatus, long lockSeconds);

  @Query(
      """
      SELECT user.id
      FROM UserEntity user
      WHERE user.status = :lockedStatus
        AND user.passwordLockedUntil <= CURRENT_TIMESTAMP
      ORDER BY user.passwordLockedUntil, user.id
      """)
  List<Long> findExpiredPasswordLockIds(Integer lockedStatus, Pageable pageable);

  @Query(
      """
      UPDATE UserEntity user
      SET user.status = :normalStatus,
          user.passwordLockedUntil = NULL
      WHERE user.id IN :userIds
        AND user.status = :lockedStatus
        AND user.passwordLockedUntil IS NOT NULL
        AND user.passwordLockedUntil <= CURRENT_TIMESTAMP
      """)
  @Modifying
  int unlockExpiredPasswordLocks(List<Long> userIds, Integer lockedStatus, Integer normalStatus);

  Optional<UserEntity> findByPhone(String loginSMS);
}
