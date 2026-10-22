package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.blog.domain.BlogVersion;

public record BlogExportPage(List<BlogExportRow> blogs) {

    public List<BlogVersion> revisions() {
        return blogs.stream().flatMap(row -> row.versions().stream()).toList();
    }

    public List<BlogExportSensitive> sensitiveContent() {
        return revisions().stream()
            .flatMap(
                revision ->
                    revision.sensitiveContent().stream()
                        .map(
                            span ->
                                new BlogExportSensitive(
                                    revision.id(),
                                    span.startIndex(),
                                    span.endIndex(),
                                    span.type())))
            .toList();
    }
}
