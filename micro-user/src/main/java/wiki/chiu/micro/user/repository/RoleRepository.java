package wiki.chiu.micro.user.repository;

import wiki.chiu.micro.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:52 am
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findByCodeIn(List<String> roles);

    List<RoleEntity> findByCodeInAndStatus(List<String> roles, Integer status);

    Optional<RoleEntity> findByCode(String role);

    @Query(value = "SELECT role.code from RoleEntity role")
    List<String> findAllCodes();
}
