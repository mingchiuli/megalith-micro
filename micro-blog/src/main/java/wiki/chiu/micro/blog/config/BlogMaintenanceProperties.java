package wiki.chiu.micro.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.CommonErrorCode;

@Component
public class BlogMaintenanceProperties {

    private final boolean readOnly;

    public BlogMaintenanceProperties(
        @Value("${megalith.blog.maintenance.read-only:false}") boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void requireWritable() {
        if (readOnly) {
            throw new BaseException(CommonErrorCode.CONFLICT, "blog writes are paused for maintenance");
        }
    }
}
