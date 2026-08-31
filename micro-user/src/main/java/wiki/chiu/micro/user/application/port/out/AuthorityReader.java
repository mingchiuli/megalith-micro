package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.user.domain.AuthorityEntity;

public interface AuthorityReader {

    List<AuthorityEntity> findAll();

    Optional<AuthorityEntity> findById(Long id);

    List<AuthorityEntity> findByIdInAndStatus(List<Long> ids, Integer status);
}
