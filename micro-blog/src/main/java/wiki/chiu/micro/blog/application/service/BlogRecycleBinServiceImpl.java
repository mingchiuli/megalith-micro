package wiki.chiu.micro.blog.application.service;

import java.util.Objects;

import wiki.chiu.micro.blog.application.model.RecycleBlogCommand;
import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.common.enums.BlogOperateEnum;

public class BlogRecycleBinServiceImpl implements BlogRecycleBin {

    private final BlogRuntimeStore runtimeStore;

    public BlogRecycleBinServiceImpl(BlogRuntimeStore runtimeStore) {
        this.runtimeStore = runtimeStore;
    }

    @Override
    public void recycle(RecycleBlogCommand command) {
        if (!BlogOperateEnum.REMOVE.equals(command.operation())) {
            return;
        }
        if (command.operatorUserId() == null) {
            return;
        }
        runtimeStore.saveDeletedBlog(
            command.operatorUserId(),
            command.eventId(),
            Objects.requireNonNull(command.blog(), "blog"));
    }
}
