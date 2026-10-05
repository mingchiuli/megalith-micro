package wiki.chiu.micro.exhibit.application.port.in;

import wiki.chiu.micro.common.message.BlogChangedMessage;

/**
 * Evicts the caches and existence entries that a blog change event invalidates once the event
 * passed the revision guard.
 */
public interface BlogChangeEviction {

    void apply(BlogChangedMessage message);
}
