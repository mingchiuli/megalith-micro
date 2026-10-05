package wiki.chiu.micro.exhibit.application.port.out;

/**
 * Drops the cached blog views that a change invalidates.
 */
public interface BlogCacheInvalidation {

    /**
     * Drops the detail and sensitive-content entries of one blog.
     *
     * @param blogId the blog whose entries must go
     */
    void invalidateBlog(Long blogId);

    /**
     * Drops every cached blog page.
     */
    void invalidatePages();
}
