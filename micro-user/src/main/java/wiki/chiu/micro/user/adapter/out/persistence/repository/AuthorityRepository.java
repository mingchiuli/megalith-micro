package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.domain.AuthorityEntity;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long>, AuthorityReader {

    List<AuthorityEntity> findByServiceHostIn(List<String> service);

    List<AuthorityEntity> findByIdInAndStatus(List<Long> ids, Integer status);
}
