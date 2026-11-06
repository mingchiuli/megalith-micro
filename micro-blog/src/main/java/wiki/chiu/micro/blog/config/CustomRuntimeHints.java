package wiki.chiu.micro.blog.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.adapter.in.http.BlogDeleteVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogDownloadReq;
import wiki.chiu.micro.blog.adapter.in.http.BlogEditVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogEntityReq;
import wiki.chiu.micro.blog.adapter.in.http.BlogEntityVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogIdVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogQueryReq;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionComparisonVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionSaveVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionSummaryVo;
import wiki.chiu.micro.blog.adapter.in.http.OssDeleteReq;
import wiki.chiu.micro.blog.adapter.in.http.PublishBlogReq;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.application.model.CollaborationTicket;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.page.PageAdapter;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types not covered by HTTP service interfaces.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                PageAdapter.class,
                CollaborationTicket.class,
                BlogRepository.BlogId.class,
                BlogEntityReq.class,
                BlogIdVo.class,
                BlogQueryReq.class,
                PublishBlogReq.class,
                BlogDownloadReq.class,
                OssDeleteReq.class,
                BlogEntityVo.class,
                BlogDeleteVo.class,
                BlogEditVo.class,
                BlogVersionSaveVo.class,
                BlogVersionSummaryVo.class,
                BlogVersionComparisonVo.class,
                BlogWorkingDraft.class);

        hints
            .reflection()
            .registerType(
                Blog.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogChangedMessage.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogAdminIndexMessage.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        hints
            .resources()
            .registerPattern("script/hot-blogs.lua")
            .registerPattern("script/blog-delete-list.lua")
            .registerPattern("script/blog-recycle.lua");
    }
}
