package wiki.chiu.micro.blog.application.port.out;

import wiki.chiu.micro.blog.application.model.BlogDraftDeleteCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogPublishCommand;
import wiki.chiu.micro.blog.application.model.BlogStatusWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogVersionWriteCommand;
import wiki.chiu.micro.blog.application.model.SavedBlogVersion;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public interface BlogAuthoringWriter {

    BlogIdentity createIdentity(Long userId);

    BlogWorkingDraft saveDraft(BlogDraftWriteCommand command);

    void deleteDraft(BlogDraftDeleteCommand command);

    SavedBlogVersion saveDraftVersion(BlogVersionWriteCommand command);

    void publish(BlogPublishCommand command);

    void changeStatus(BlogStatusWriteCommand command);
}
