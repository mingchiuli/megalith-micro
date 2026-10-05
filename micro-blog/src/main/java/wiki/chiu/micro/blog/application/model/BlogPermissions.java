package wiki.chiu.micro.blog.application.model;

/**
 * What the caller may do with a blog.
 */
public record BlogPermissions(
    boolean collaborate, boolean commit, boolean manageMetadata, boolean manageAssets) {
}
