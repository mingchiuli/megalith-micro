package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;

/**
 * Internal strategy that applies one kind of blog change. This is deliberately not an input port:
 * the single {@link BlogChangeEvictionServiceImpl} use case owns the dispatch.
 */
public interface BlogChangeEvictionHandler {

    BlogOperateEnum operation();

    void applyChange(BlogChangedMessage message);
}
