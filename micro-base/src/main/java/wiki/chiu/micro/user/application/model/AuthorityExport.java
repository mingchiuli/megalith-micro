package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;

/**
 * The rows the authority export delivers: the authorities with their menu bindings.
 */
public record AuthorityExport(List<Authority> authorities, List<MenuAuthority> menuAuthorities) {
}
