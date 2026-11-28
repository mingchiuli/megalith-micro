package wiki.chiu.micro.blog.application.model;

import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;

/**
 * Whether the blog table is open for writes. The flag reaches the adapters as this value rather
 * than as a configuration class, so adapters never depend on the composition root.
 */
public record BlogMaintenanceMode(boolean readOnly) {

    public void requireWritable() {
        if (readOnly) {
            throw new BaseException(CommonErrorCode.CONFLICT, "blog writes are paused for maintenance");
        }
    }
}
