package wiki.chiu.micro.common.model;

/**
 * A complete database projection used to initialize the current administration index.
 */
public record BlogAdminIndexSnapshot(
    BlogAdminSnapshot blog,
    BlogDraftSnapshot draft,
    BlogRevisionSnapshot revision) {
}
