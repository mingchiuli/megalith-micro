package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.blog.domain.BlogVersion;

public record BlogExportPage(List<BlogExportRow> blogs) {

    public List<BlogVersion> revisions() {
        return blogs.stream().flatMap(row -> row.versions().stream()).toList();
    }
}
