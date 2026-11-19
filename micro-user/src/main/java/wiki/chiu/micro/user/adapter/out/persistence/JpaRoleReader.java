package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.convertor.UserPersistenceConvertor;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleEntity;
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
        return roles.findById(id).map(UserPersistenceConvertor::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return roles.findByCode(code).map(UserPersistenceConvertor::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return UserPersistenceConvertor.toRoles(roles.findAll());
    }

    @Override
    public List<Role> findAllById(Iterable<Long> ids) {
        return UserPersistenceConvertor.toRoles(roles.findAllById(ids));
    }

    @Override
    public List<Role> findByCodeIn(List<String> codes) {
        return UserPersistenceConvertor.toRoles(roles.findByCodeIn(codes));
    }

    @Override
    public List<Role> findByStatus(Integer status) {
        return UserPersistenceConvertor.toRoles(roles.findByStatus(status));
    }

    @Override
    public Page<Role> findPage(int pageNumber, int pageSize) {
        Page<RoleEntity> page = roles.findPage(pageNumber, pageSize);
        return page.withContent(UserPersistenceConvertor.toRoles(page.content()));
    }
}
