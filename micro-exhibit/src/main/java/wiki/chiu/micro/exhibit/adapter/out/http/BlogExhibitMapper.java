package wiki.chiu.micro.exhibit.adapter.out.http;

import wiki.chiu.micro.blog.api.vo.BlogPublicationContentRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogPublicationRpcVo;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

/**
 * Turns the blog and user service payloads into the exhibit service's own model.
 */
public final class BlogExhibitMapper {

    private BlogExhibitMapper() {
    }

    public static BlogExhibit toModel(
        BlogPublicationRpcVo publication, BlogPublicationContentRpcVo content, UserEntityRpcVo author) {
        int status = publication.accessMode() == BlogAccessMode.RESTRICTED
            ? BlogStatusEnum.HIDE.getCode() : content.renderMode().getCode();
        return new BlogExhibit(
            publication.userId(), content.description(), author.nickname(), author.avatar(),
            content.title(), content.content(),
            publication.publishedAt() == null ? publication.created() : publication.publishedAt(),
            publication.readCount(), status, content.sensitiveContent());
    }
}
