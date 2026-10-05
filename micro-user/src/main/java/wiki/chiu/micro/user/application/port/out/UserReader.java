package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.domain.User;

public interface UserReader {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);

    List<User> findAll();

    List<Long> findExpiredPasswordLockIds(Integer lockedStatus, int batchSize);

    PageAdapter<User> findPage(int pageNumber, int pageSize);
}
