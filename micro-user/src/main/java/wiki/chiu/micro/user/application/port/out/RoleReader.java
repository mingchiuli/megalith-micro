package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.domain.RoleEntity;

public interface RoleReader {

    Optional<RoleEntity> findById(Long id);

    Optional<RoleEntity> findByCode(String code);

    List<RoleEntity> findAll();

    List<RoleEntity> findAllById(Iterable<Long> ids);

    List<RoleEntity> findByCodeIn(List<String> codes);

    List<RoleEntity> findByCodeInAndStatus(List<String> codes, Integer status);

    PageAdapter<RoleEntity> findPage(int pageNumber, int pageSize);
}
