package org.chiu.micro.user.wrapper;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserRoleWrapper {

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    @Transactional
    public void saveOrUpdate(UserEntity userEntity, List<UserRoleEntity> userRoleEntities) {
        userRoleRepository.deleteByUserId(userEntity.getId());
        userRoleRepository.flush();
        userRepository.save(userEntity);
        userRoleRepository.saveAll(userRoleEntities);
    }

    @Transactional
    public void deleteUsers(List<Long> ids) {
        userRepository.deleteAllById(ids);
        userRoleRepository.deleteByUserIdIn(ids);
    }
}
