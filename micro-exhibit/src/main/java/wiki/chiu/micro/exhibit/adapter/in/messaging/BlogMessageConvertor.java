package wiki.chiu.micro.exhibit.adapter.in.messaging;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.application.model.BlogChange;

public final class BlogMessageConvertor {

    private BlogMessageConvertor() {
    }

    public static BlogChange toCommand(BlogChangedMessage message) {
        return new BlogChange(message.blogSnapshot().id(), BlogOperateEnum.of(message.operation()),
            message.revision(), message.blogSnapshot().status());
    }
}
