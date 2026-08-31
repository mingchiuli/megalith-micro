package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;

public interface AuthorityWriter {

    void saveAuthority(Long menuId, List<MenuAuthorityEntity> authorities, List<Long> roleIds);

    void deleteAuthorities(List<Long> ids, List<Long> roleIds);

    void saveAuthorityEntity(AuthorityEntity authority, List<Long> roleIds);
}
