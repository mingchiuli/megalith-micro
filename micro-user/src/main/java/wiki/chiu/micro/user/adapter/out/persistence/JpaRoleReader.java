package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleEntity;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleRepository;
import wiki.chiu.micro.user.application.model.Page;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.Role;

@Component
public class JpaRoleReader implements RoleReader {

    private final RoleRepository roles;

    public JpaRoleReader(RoleRepository roles) {
        this.roles = roles;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roles.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return roles.findByCode(code).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return UserPersistenceMapper.toRoles(roles.findAll());
    }

    @Override
    public List<Role> findAllById(Iterable<Long> ids) {
        return UserPersistenceMapper.toRoles(roles.findAllById(ids));
    }

    @Override
    public List<Role> findByCodeIn(List<String> codes) {
        return UserPersistenceMapper.toRoles(roles.findByCodeIn(codes));
    }

    @Override
    public List<Role> findByCodeInAndStatus(List<String> codes, Integer status) {
        return UserPersistenceMapper.toRoles(roles.findByCodeInAndStatus(codes, status));
    }

    @Override
    public List<Role> findByStatus(Integer status) {
        return UserPersistenceMapper.toRoles(roles.findByStatus(status));
    }

    @Override
    public Page<Role> findPage(int pageNumber, int pageSize) {
        Page<RoleEntity> page = roles.findPage(pageNumber, pageSize);
        return page.withContent(UserPersistenceMapper.toRoles(page.content()));
    }
}
