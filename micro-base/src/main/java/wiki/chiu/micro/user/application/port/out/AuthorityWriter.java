package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;

public interface AuthorityWriter {

    void saveAuthority(Long menuId, List<MenuAuthority> authorities, List<Long> roleIds);

    void deleteAuthorities(List<Long> ids, List<Long> roleIds);

    void saveAuthorityEntity(Authority authority, List<Long> roleIds);
}
