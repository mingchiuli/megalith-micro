package wiki.chiu.micro.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import wiki.chiu.micro.user.entity.UserEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:53 am
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  interface UserAccessRow {
    Long getUserId();

    Integer getStatus();

    Long getRoleId();
  }

  @Query(
      value =
          """
          SELECT u.id AS userId, u.status AS status, user_role.role_id AS roleId
          FROM m_user u
          LEFT JOIN m_user_role user_role ON user_role.user_id = u.id
          WHERE u.id = :userId
          """,
      nativeQuery = true)
  List<UserAccessRow> findAccessRows(Long userId);

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

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      SELECT user
      FROM UserEntity user
      WHERE user.status = :lockedStatus
        AND user.passwordLockedUntil <= CURRENT_TIMESTAMP
      ORDER BY user.passwordLockedUntil, user.id
      """)
  List<UserEntity> findExpiredPasswordLocks(Integer lockedStatus, Pageable pageable);

  Optional<UserEntity> findByPhone(String loginSMS);
}
