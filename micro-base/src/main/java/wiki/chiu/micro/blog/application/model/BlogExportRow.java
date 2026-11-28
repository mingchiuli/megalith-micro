package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;

public record BlogExportRow(BlogIdentity blog, List<BlogVersion> versions) {
}
