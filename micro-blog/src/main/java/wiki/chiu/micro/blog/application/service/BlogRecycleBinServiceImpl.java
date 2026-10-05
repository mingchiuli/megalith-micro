package wiki.chiu.micro.blog.application.service;

import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;

public class BlogRecycleBinServiceImpl implements BlogRecycleBin {

    private final BlogRuntimeStore runtimeStore;

    public BlogRecycleBinServiceImpl(BlogRuntimeStore runtimeStore) {
        this.runtimeStore = runtimeStore;
    }

    @Override
    public void recycle(BlogChangedMessage event) {
        if (!BlogOperateEnum.REMOVE.equals(BlogOperateEnum.of(event.operation()))) {
            return;
        }
        if (event.operatorUserId() == null) {
            return;
        }
        runtimeStore.saveDeletedBlog(
            event.operatorUserId(), event.eventId(), event.blogSnapshot());
    }
}
