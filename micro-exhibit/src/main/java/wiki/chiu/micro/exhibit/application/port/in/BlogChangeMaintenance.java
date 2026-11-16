package wiki.chiu.micro.exhibit.application.port.in;

import wiki.chiu.micro.exhibit.application.model.BlogChange;

/**
 * Maintains existence, read tokens, and weekly counts after a blog change event
 * passes the revision guard.
 */
public interface BlogChangeMaintenance {

    void apply(BlogChange change);
}
