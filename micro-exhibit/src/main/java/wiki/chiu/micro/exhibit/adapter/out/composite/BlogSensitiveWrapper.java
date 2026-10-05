package wiki.chiu.micro.exhibit.adapter.out.composite;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.enums.SensitiveTypeEnum;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.application.port.out.SensitiveContentReader;
import wiki.chiu.micro.exhibit.domain.BlogCacheDescriptors;
import wiki.chiu.micro.exhibit.domain.SensitiveSpan;

@Component
public class BlogSensitiveWrapper implements SensitiveContentReader {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    public BlogSensitiveWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    @Cache(
        namespace = BlogCacheDescriptors.SENSITIVE_NAMESPACE,
        version = BlogCacheDescriptors.VERSION)
    @Override
    public List<SensitiveSpan> findSensitiveSpans(Long blogId) {
        List<SensitiveSpan> spans = new ArrayList<>();
        for (SensitiveContentRpcVo span :
            blogHttpServiceWrapper.findSensitiveByBlogId(blogId).sensitiveContent()) {
            SensitiveTypeEnum.ofCode(span.type())
                .ifPresent(
                    type -> spans.add(new SensitiveSpan(type, span.startIndex(), span.endIndex())));
        }
        return List.copyOf(spans);
    }
}
