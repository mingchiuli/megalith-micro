package wiki.chiu.micro.common.arch.fixture.value.domain;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record SharedValue(BlogRenderMode mode, BlogSensitiveSpan span) {
}
