package wiki.chiu.micro.user.application.model;

/**
 * An authority offered to one menu, flagged with whether the menu already uses it.
 */
public record MenuAuthorityView(Long authorityId, String code, boolean selected) {
}
