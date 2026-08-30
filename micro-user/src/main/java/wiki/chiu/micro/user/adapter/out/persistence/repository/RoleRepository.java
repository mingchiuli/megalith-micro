package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.RoleEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:52 am
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long>, RoleReader {

  List<RoleEntity> findByCodeIn(List<String> roles);

  List<RoleEntity> findByCodeInAndStatus(List<String> roles, Integer status);

  Optional<RoleEntity> findByCode(String role);

  @Query(value = "SELECT role.code from RoleEntity role")
  List<String> findAllCodes();

  @Override
  default PageAdapter<RoleEntity> findPage(int pageNumber, int pageSize) {
    var request = PageRequest.of(pageNumber - 1, pageSize, Sort.by("created").ascending());
    var page = findAll(request);
    return PageAdapter.<RoleEntity>builder()
        .content(page.getContent())
        .totalElements(page.getTotalElements())
        .pageNumber(page.getNumber())
        .pageSize(page.getSize())
        .first(page.isFirst())
        .last(page.isLast())
        .empty(page.isEmpty())
        .totalPages(page.getTotalPages())
        .build();
  }
}
