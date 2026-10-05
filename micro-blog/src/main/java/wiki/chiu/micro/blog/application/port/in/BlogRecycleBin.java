package wiki.chiu.micro.blog.application.port.in;

import wiki.chiu.micro.common.message.BlogChangedMessage;

/**
 * Keeps the recycle bin in sync with blog change events: a blog removed by its owner is parked
 * there, while cascade deletes without an operator are ignored.
 */
public interface BlogRecycleBin {

    void recycle(BlogChangedMessage event);
}
