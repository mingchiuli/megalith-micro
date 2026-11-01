package wiki.chiu.micro.common.model;

public record BlogAdminIndexSnapshot(
    BlogAdminSnapshot blog,
    BlogDraftSnapshot draft,
    BlogRevisionSnapshot revision,
    BlogRevisionSnapshot publishedRevision) {
}
