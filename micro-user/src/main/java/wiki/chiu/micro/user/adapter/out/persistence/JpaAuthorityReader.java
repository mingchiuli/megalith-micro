package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.AuthorityRepository;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.domain.Authority;

@Component
public class JpaAuthorityReader implements AuthorityReader {

    private final AuthorityRepository authorities;

    public JpaAuthorityReader(AuthorityRepository authorities) {
        this.authorities = authorities;
    }

    @Override
    public List<Authority> findAll() {
        return UserPersistenceMapper.toAuthorities(authorities.findAll());
    }

    @Override
    public Optional<Authority> findById(Long id) {
        return authorities.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<Authority> findByStatus(Integer status) {
        return UserPersistenceMapper.toAuthorities(authorities.findByStatus(status));
    }

    @Override
    public List<Authority> findByStatusAndType(Integer status, Integer type) {
        return UserPersistenceMapper.toAuthorities(authorities.findByStatusAndType(status, type));
    }

    @Override
    public List<Authority> findByIdInAndStatus(List<Long> ids, Integer status) {
        return UserPersistenceMapper.toAuthorities(authorities.findByIdInAndStatus(ids, status));
    }
}
