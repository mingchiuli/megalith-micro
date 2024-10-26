package wiki.chiu.micro.user.repository;

import wiki.chiu.micro.user.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long> {

    List<AuthorityEntity> findByStatus(Integer status);

    List<AuthorityEntity> findByServiceHostInAndStatus(List<String> service, Integer status);

}
