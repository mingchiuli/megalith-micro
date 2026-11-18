package wiki.chiu.micro.exhibit.adapter.out.composite;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.vo.BlogPublicationContentRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogPublicationRpcVo;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogExhibitMapper;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpMapper;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.RecommendationFeed;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.vo.RecommendationFeedRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@Component
public class BlogWrapper implements BlogReader {

    private final SearchHttpService searchHttpService;

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final TaskExecutor taskExecutor;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public BlogWrapper(
        SearchHttpService searchHttpService,
        BlogHttpServiceWrapper blogHttpServiceWrapper,
        UserHttpServiceWrapper userHttpServiceWrapper,
        @Qualifier("commonExecutor") TaskExecutor taskExecutor) {
        this.searchHttpService = searchHttpService;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public BlogExhibit findById(Long id) {
        BlogPublicationRpcVo publication = blogHttpServiceWrapper.publication(id);
        BlogPublicationContentRpcVo content;
        try {
            content = blogHttpServiceWrapper.publicationContent(id, publication.revisionId());
        } catch (RemoteServiceException failure) {
            if (failure.errorCode() != CommonErrorCode.CONFLICT) {
                throw failure;
            }
            publication = blogHttpServiceWrapper.publication(id);
            content = blogHttpServiceWrapper.publicationContent(id, publication.revisionId());
        }

        UserEntityRpcVo user = userHttpServiceWrapper.findById(publication.userId());
        return BlogExhibitMapper.toModel(publication, content, user);
    }

    @Override
    public void incrementViews(Long id) {
        taskExecutor.execute(() -> blogHttpServiceWrapper.setReadCount(id));
    }

    @Override
    public RecommendationFeed recommendationFeed(String cursor) {
        RecommendationFeedRpcVo page =
            RemoteResult.requireSuccess(
                () -> searchHttpService.recommendationFeed(cursor, blogPageSize));
        return BlogHttpMapper.toRecommendationFeed(page);
    }
}
