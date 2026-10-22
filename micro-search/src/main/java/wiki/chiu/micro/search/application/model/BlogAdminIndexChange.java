package wiki.chiu.micro.search.application.model;

import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;

public record BlogAdminIndexChange(
    String operation,
    BlogAdminSnapshot blog,
    BlogDraftSnapshot draft,
    BlogRevisionSnapshot revision) {
}
