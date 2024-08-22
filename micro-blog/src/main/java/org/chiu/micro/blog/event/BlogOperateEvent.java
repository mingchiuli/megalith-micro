package org.chiu.micro.blog.event;

import lombok.Getter;
import lombok.Setter;
import org.chiu.micro.blog.constant.BlogOperateMessage;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class BlogOperateEvent extends ApplicationEvent {

    private BlogOperateMessage blogOperateMessage;

    public BlogOperateEvent(Object source, BlogOperateMessage blogOperateMessage) {
        super(source);
        this.blogOperateMessage = blogOperateMessage;
    }
}
