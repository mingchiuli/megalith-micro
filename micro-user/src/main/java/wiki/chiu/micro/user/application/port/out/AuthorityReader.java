package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.user.domain.Authority;

public interface AuthorityReader {

    List<Authority> findAll();

    Optional<Authority> findById(Long id);

    List<Authority> findByStatus(Integer status);

    List<Authority> findByStatusAndType(Integer status, Integer type);

    List<Authority> findByIdInAndStatus(List<Long> ids, Integer status);
}
