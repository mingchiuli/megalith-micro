package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.user.adapter.out.persistence.entity.UserEntity;
import wiki.chiu.micro.user.application.model.Page;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:53 am
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Modifying
    @Query(
        """
        UPDATE UserEntity entity
        SET entity.username = :username,
            entity.nickname = :nickname,
            entity.avatar = :avatar,
            entity.email = :email,
            entity.phone = :phone,
            entity.password = :password,
            entity.status = :status,
            entity.passwordLockedUntil = :passwordLockedUntil,
            entity.updated = :updated
        WHERE entity.id = :id
        """)
    int updateDetails(
        @Param("id") Long id,
        @Param("username") String username,
        @Param("nickname") String nickname,
        @Param("avatar") String avatar,
        @Param("email") String email,
        @Param("phone") String phone,
        @Param("password") String password,
        @Param("status") Integer status,
        @Param("passwordLockedUntil") LocalDateTime passwordLockedUntil,
        @Param("updated") LocalDateTime updated);

    default int updateDetails(UserEntity entity, LocalDateTime updated) {
        return updateDetails(
            entity.getId(),
            entity.getUsername(),
            entity.getNickname(),
            entity.getAvatar(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getPassword(),
            entity.getStatus(),
            entity.getPasswordLockedUntil(),
            updated);
    }

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByUsernameOrEmailOrPhone(String username, String email, String phone);

    @Query(
        value =
            "UPDATE UserEntity user set user.lastLogin = ?2 where (user.username = ?1 or user.email = ?1 or user.phone = ?1)")
    @Modifying
    void updateLoginTime(String username, LocalDateTime time);

    // MariaDB's local_datetime rendering loses microseconds; cast the dialect's current timestamp instead.
    @Query(
        """
        UPDATE UserEntity user
        SET user.status = :lockedStatus,
            user.passwordLockedUntil = timestampadd(second, :lockSeconds, cast(CURRENT_TIMESTAMP as LocalDateTime))
        WHERE user.id = :userId
          AND user.status = :normalStatus
          AND user.passwordLockedUntil IS NULL
        """)
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

    default List<Long> findExpiredPasswordLockIds(Integer lockedStatus, int batchSize) {
        return findExpiredPasswordLockIds(lockedStatus, PageRequest.of(0, batchSize));
    }

    default Page<UserEntity> findPage(int pageNumber, int pageSize) {
        var request = PageRequest.of(pageNumber - 1, pageSize, Sort.by("created", "id").ascending());
        var page = findAll(request);
        return new Page<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty(),
            page.getTotalPages());
    }
}
