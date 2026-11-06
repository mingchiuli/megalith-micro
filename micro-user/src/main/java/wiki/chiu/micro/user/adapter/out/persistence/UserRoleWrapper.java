package wiki.chiu.micro.user.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.adapter.out.messaging.UserDeletionOutbox;
import wiki.chiu.micro.user.adapter.out.persistence.entity.UserEntity;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.application.port.out.UserWriter;
import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.domain.UserRole;

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
    public void saveOrUpdate(User user, List<UserRole> roles) {
        UserEntity entity = UserPersistenceMapper.toEntity(user);
        Long userId;
        if (user.id() == null) {
            userId = userRepository.save(entity).getId();
        } else {
            userId = user.id();
            if (userRepository.updateDetails(entity, LocalDateTime.now()) != 1) {
                throw new BaseException(CommonErrorCode.CONFLICT, "user changed: " + userId);
            }
            userRoleRepository.deleteByUserId(userId);
        }
        userRoleRepository.saveAll(
            roles.stream()
                .map(role -> UserPersistenceMapper.toEntity(role.onUser(userId)))
                .toList());
        cacheEvictions.enqueue(List.of(userId), List.of(), List.of(), false, false);
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
