package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

/**
 * @Author limingjiu @Date 2024/5/29 13:41
 */
@Component
public class UserRoleWrapper {

  private final UserRepository userRepository;

  private final UserRoleRepository userRoleRepository;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public UserRoleWrapper(
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      AuthCacheEvictionOutbox cacheEvictions) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveOrUpdate(UserEntity userEntity, List<UserRoleEntity> userRoleEntities) {
    userRoleRepository.deleteByUserId(userEntity.getId());
    userRepository.save(userEntity);
    userRoleEntities.forEach(userRole -> userRole.setUserId(userEntity.getId()));
    userRoleRepository.saveAll(userRoleEntities);
    cacheEvictions.enqueue(List.of(userEntity.getId()), List.of(), List.of(), false, false);
  }

  @Transactional
  public void deleteUsers(List<Long> ids) {
    userRepository.deleteAllById(ids);
    userRoleRepository.deleteByUserIdIn(ids);
    cacheEvictions.enqueue(ids, List.of(), List.of(), false, false);
  }
}
