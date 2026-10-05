package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.UserRole;

@Component
public class JpaUserRoleReader implements UserRoleReader {

    private final UserRoleRepository userRoles;

    public JpaUserRoleReader(UserRoleRepository userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public List<UserRole> findAll() {
        return UserPersistenceMapper.toUserRoles(userRoles.findAll());
    }

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return UserPersistenceMapper.toUserRoles(userRoles.findByUserId(userId));
    }

    @Override
    public List<UserRole> findByUserIdIn(List<Long> userIds) {
        return UserPersistenceMapper.toUserRoles(userRoles.findByUserIdIn(userIds));
    }
}
