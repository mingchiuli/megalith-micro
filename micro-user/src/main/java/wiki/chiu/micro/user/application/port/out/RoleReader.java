package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.user.application.model.Page;
import wiki.chiu.micro.user.domain.Role;

public interface RoleReader {

    Optional<Role> findById(Long id);

    Optional<Role> findByCode(String code);

    List<Role> findAll();

    List<Role> findAllById(Iterable<Long> ids);

    List<Role> findByCodeIn(List<String> codes);

    List<Role> findByCodeInAndStatus(List<String> codes, Integer status);

    List<Role> findByStatus(Integer status);

    Page<Role> findPage(int pageNumber, int pageSize);
}
