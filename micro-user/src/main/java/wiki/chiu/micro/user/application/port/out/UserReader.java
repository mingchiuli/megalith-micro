package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.domain.UserEntity;

public interface UserReader {

  Optional<UserEntity> findById(Long id);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByPhone(String phone);

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByUsernameOrEmailOrPhone(String username, String email, String phone);

  List<UserEntity> findAll();

  List<Long> findExpiredPasswordLockIds(Integer lockedStatus, int batchSize);

  PageAdapter<UserEntity> findPage(int pageNumber, int pageSize);
}
