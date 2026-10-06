package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleEntity;
import wiki.chiu.micro.user.application.model.Page;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:52 am
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findByCodeIn(List<String> roles);

    List<RoleEntity> findByCodeInAndStatus(List<String> roles, Integer status);

    Optional<RoleEntity> findByCode(String role);

    List<RoleEntity> findByStatus(Integer status);

    default Page<RoleEntity> findPage(int pageNumber, int pageSize) {
        var request = PageRequest.of(pageNumber - 1, pageSize, Sort.by("created").ascending());
        var page = findAll(request);
        return new Page<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty(),
            page.getTotalPages());
    }
}
