package wiki.chiu.micro.blog.application.service;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogSensitiveSpans;
import wiki.chiu.micro.blog.application.model.SensitiveContentSpan;
import wiki.chiu.micro.blog.application.port.in.BlogSensitiveService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.SensitiveContent;

public class BlogSensitiveServiceImpl implements BlogSensitiveService {

    private final BlogQueryStore blogs;

    public BlogSensitiveServiceImpl(BlogQueryStore blogs) {
        this.blogs = blogs;
    }

    @Override
    public BlogSensitiveSpans findByBlogId(Long blogId) {
        List<SensitiveContent> spans = blogs.findSensitiveByBlogId(blogId);
        Long owner = spans.isEmpty() ? null : spans.getFirst().blogId();
        return new BlogSensitiveSpans(
            owner,
            spans.stream()
                .map(
                    span ->
                        new SensitiveContentSpan(span.startIndex(), span.endIndex(), span.type()))
                .toList());
    }
}
