package wiki.chiu.micro.common.arch.fixture.message.application.model;

import wiki.chiu.micro.common.message.BlogChangedMessage;

public record MessageCommand(BlogChangedMessage event) {
}
