package wiki.chiu.micro.exhibit.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.exhibit.application.port.in.BlogChangeEviction;

public class BlogChangeEvictionServiceImpl implements BlogChangeEviction {

    private final Map<BlogOperateEnum, BlogChangeEvictionHandler> handlers;

    public BlogChangeEvictionServiceImpl(List<BlogChangeEvictionHandler> handlers) {
        this.handlers =
            handlers.stream()
                .collect(
                    Collectors.toUnmodifiableMap(
                        BlogChangeEvictionHandler::operation,
                        Function.identity(),
                        (left, _) -> {
                            throw new IllegalStateException(
                                "Duplicate blog change eviction handler for " + left.operation());
                        }));
    }

    @Override
    public void apply(BlogChangedMessage message) {
        BlogOperateEnum operation = BlogOperateEnum.of(message.operation());
        BlogChangeEvictionHandler handler = handlers.get(operation);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
        handler.applyChange(message);
    }
}
