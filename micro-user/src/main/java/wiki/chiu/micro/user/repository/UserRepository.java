package wiki.chiu.micro.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
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
  @Transactional
  void updateLoginTime(String username, LocalDateTime time);

  @Query(value = "UPDATE UserEntity user set user.status = :status where user.username = :username")
  @Modifying
  @Transactional
  void updateUserStatusByUsername(String username, Integer status);

  Optional<UserEntity> findByPhone(String loginSMS);
}
