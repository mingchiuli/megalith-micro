package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.adapter.out.persistence.entity.UserEntity;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRepository;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.domain.User;

@Component
public class JpaUserReader implements UserReader {

    private final UserRepository users;

    public JpaUserReader(UserRepository users) {
        this.users = users;
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return users.findByPhone(phone).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.findByUsername(username).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmailOrPhone(
        String username, String email, String phone) {
        return users
            .findByUsernameOrEmailOrPhone(username, email, phone)
            .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return UserPersistenceMapper.toUsers(users.findAll());
    }

    @Override
    public List<Long> findExpiredPasswordLockIds(Integer lockedStatus, int batchSize) {
        return users.findExpiredPasswordLockIds(lockedStatus, batchSize);
    }

    @Override
    public PageAdapter<User> findPage(int pageNumber, int pageSize) {
        PageAdapter<UserEntity> page = users.findPage(pageNumber, pageSize);
        return PageAdapter.<User>builder()
            .content(UserPersistenceMapper.toUsers(page.content()))
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }
}
