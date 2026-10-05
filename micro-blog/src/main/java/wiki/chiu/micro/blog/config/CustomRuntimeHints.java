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
import wiki.chiu.micro.blog.adapter.in.http.BlogPermissionsVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogQueryReq;
import wiki.chiu.micro.blog.adapter.in.http.OssDeleteReq;
import wiki.chiu.micro.blog.adapter.in.http.SensitiveContentReq;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types bound by the functional routes in adapter.in.http.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                Result.class,
                PageAdapter.class,
                BlogEntityReq.class,
                SensitiveContentReq.class,
                BlogQueryReq.class,
                BlogDownloadReq.class,
                OssDeleteReq.class,
                BlogEntityVo.class,
                BlogDeleteVo.class,
                BlogEditVo.class,
                BlogPermissionsVo.class,
                BlogEntityRpcVo.class,
                BlogSensitiveContentRpcVo.class,
                BlogIndexSourceStatus.class,
                BlogSnapshot.class);

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
                BlogSnapshot.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        hints
            .resources()
            .registerPattern("script/hot-blogs.lua")
            .registerPattern("script/blog-delete-list.lua")
            .registerPattern("script/blog-recycle.lua");
    }
}
