package org.chiu.micro.user.wrapper;

import org.chiu.micro.user.entity.UserEntity;
import org.chiu.micro.user.entity.UserRoleEntity;
import org.chiu.micro.user.repository.UserRepository;
import org.chiu.micro.user.repository.UserRoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/5/29 13:41
 **/
@Component
public class UserRoleWrapper {

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    public UserRoleWrapper(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public void saveOrUpdate(UserEntity userEntity, List<UserRoleEntity> userRoleEntities) {
        userRoleRepository.deleteByUserId(userEntity.getId());
        userRepository.save(userEntity);
        userRoleEntities.forEach(userRole -> userRole.setUserId(userEntity.getId()));
        userRoleRepository.saveAll(userRoleEntities);
    }

    @Transactional
    public void deleteUsers(List<Long> ids) {
        userRepository.deleteAllById(ids);
        userRoleRepository.deleteByUserIdIn(ids);
    }
}
