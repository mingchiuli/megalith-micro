package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.application.port.out.UserWriter;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.support.UserDeletionOutbox;

/**
 * @Author limingjiu @Date 2024/5/29 13:41
 */
@Component
public class UserRoleWrapper implements UserWriter {

  private final UserRepository userRepository;

  private final UserRoleRepository userRoleRepository;
  private final AuthCacheEvictionOutbox cacheEvictions;
  private final UserDeletionOutbox userDeletions;

  public UserRoleWrapper(
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      AuthCacheEvictionOutbox cacheEvictions,
      UserDeletionOutbox userDeletions) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.cacheEvictions = cacheEvictions;
    this.userDeletions = userDeletions;
  }

  @Transactional
  @Override
  public void saveOrUpdate(UserEntity userEntity, List<UserRoleEntity> userRoleEntities) {
    userRoleRepository.deleteByUserId(userEntity.getId());
    userRepository.save(userEntity);
    userRoleEntities.forEach(userRole -> userRole.setUserId(userEntity.getId()));
    userRoleRepository.saveAll(userRoleEntities);
    cacheEvictions.enqueue(List.of(userEntity.getId()), List.of(), List.of(), false, false);
  }

  @Transactional
  @Override
  public void deleteUsers(List<Long> ids) {
    userRoleRepository.deleteByUserIdIn(ids);
    userRepository.deleteAllByIdInBatch(ids);
    cacheEvictions.enqueue(ids, List.of(), List.of(), false, false);
    userDeletions.enqueue(ids);
  }
}
