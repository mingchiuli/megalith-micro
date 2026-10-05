package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.user.application.model.AuthorityDraft;
import wiki.chiu.micro.user.application.model.AuthorityExport;
import wiki.chiu.micro.user.domain.Authority;

public interface AuthorityService {

    List<Authority> findAllByService();

    List<Authority> findAll();

    Authority findById(Long id);

    void saveOrUpdate(AuthorityDraft req);

    void deleteAuthorities(List<Long> ids);

    AuthorityExport export();
}
