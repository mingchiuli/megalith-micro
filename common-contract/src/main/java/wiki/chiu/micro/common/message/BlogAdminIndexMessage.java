package wiki.chiu.micro.common.message;

import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;

public record BlogAdminIndexMessage(
    String eventId,
    String operation,
    BlogAdminSnapshot blog,
    BlogDraftSnapshot draft,
    BlogRevisionSnapshot revision) {
}
