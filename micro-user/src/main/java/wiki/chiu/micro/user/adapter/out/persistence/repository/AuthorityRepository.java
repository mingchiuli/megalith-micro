package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.user.adapter.out.persistence.entity.AuthorityEntity;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long> {

    @Modifying
    @Query(
        """
        UPDATE AuthorityEntity entity
        SET entity.code = :code,
            entity.remark = :remark,
            entity.protocol = :protocol,
            entity.methodType = :methodType,
            entity.routePattern = :routePattern,
            entity.serviceHost = :serviceHost,
            entity.servicePort = :servicePort,
            entity.type = :type,
            entity.status = :status,
            entity.updated = :updated
        WHERE entity.id = :id
        """)
    int updateDetails(
        @Param("id") Long id,
        @Param("code") String code,
        @Param("remark") String remark,
        @Param("protocol") String protocol,
        @Param("methodType") String methodType,
        @Param("routePattern") String routePattern,
        @Param("serviceHost") String serviceHost,
        @Param("servicePort") Integer servicePort,
        @Param("type") Integer type,
        @Param("status") Integer status,
        @Param("updated") LocalDateTime updated);

    default int updateDetails(AuthorityEntity entity, LocalDateTime updated) {
        return updateDetails(
            entity.getId(),
            entity.getCode(),
            entity.getRemark(),
            entity.getProtocol(),
            entity.getMethodType(),
            entity.getRoutePattern(),
            entity.getServiceHost(),
            entity.getServicePort(),
            entity.getType(),
            entity.getStatus(),
            updated);
    }

    List<AuthorityEntity> findByStatus(Integer status);

    List<AuthorityEntity> findByStatusAndType(Integer status, Integer type);

    List<AuthorityEntity> findByIdInAndStatus(List<Long> ids, Integer status);
}
