package wiki.chiu.micro.blog.event;

import wiki.chiu.micro.blog.constant.BlogOperateMessage;
import org.springframework.context.ApplicationEvent;

public class BlogOperateEvent extends ApplicationEvent {

    private BlogOperateMessage blogOperateMessage;

    public BlogOperateEvent(Object source, BlogOperateMessage blogOperateMessage) {
        super(source);
        this.blogOperateMessage = blogOperateMessage;
    }

    public BlogOperateMessage getBlogOperateMessage() {
        return this.blogOperateMessage;
    }

    public void setBlogOperateMessage(BlogOperateMessage blogOperateMessage) {
        this.blogOperateMessage = blogOperateMessage;
    }
}
