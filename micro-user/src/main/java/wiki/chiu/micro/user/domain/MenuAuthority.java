package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * The link between a menu entry and an authority.
 */
public record MenuAuthority(
    Long id, Long menuId, Long authorityId, LocalDateTime created, LocalDateTime updated) {

    public MenuAuthority onMenu(Long newMenuId) {
        return new MenuAuthority(id, newMenuId, authorityId, created, updated);
    }
}
