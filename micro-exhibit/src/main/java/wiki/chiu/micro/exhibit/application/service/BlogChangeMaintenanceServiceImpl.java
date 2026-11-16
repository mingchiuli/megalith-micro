package wiki.chiu.micro.exhibit.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.in.BlogChangeMaintenance;

public class BlogChangeMaintenanceServiceImpl implements BlogChangeMaintenance {

    private final Map<BlogOperateEnum, BlogChangeMaintenanceHandler> handlers;

    public BlogChangeMaintenanceServiceImpl(List<BlogChangeMaintenanceHandler> handlers) {
        this.handlers =
            handlers.stream()
                .collect(
                    Collectors.toUnmodifiableMap(
                        BlogChangeMaintenanceHandler::operation,
                        Function.identity(),
                        (left, _) -> {
                            throw new IllegalStateException(
                                "Duplicate blog change maintenance handler for " + left.operation());
                        }));
    }

    @Override
    public void apply(BlogChange change) {
        BlogOperateEnum operation = change.operation();
        BlogChangeMaintenanceHandler handler = handlers.get(operation);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
        handler.applyChange(change);
    }
}
