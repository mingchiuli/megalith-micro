package wiki.chiu.micro.search.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.search.adapter.in.http.BlogDocumentVo;
import wiki.chiu.micro.search.adapter.out.elasticsearch.AdminBlogDocument;
import wiki.chiu.micro.search.adapter.out.elasticsearch.AdminRevisionDocument;
import wiki.chiu.micro.search.adapter.out.elasticsearch.BlogDocument;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;
import wiki.chiu.micro.search.api.vo.AdminBlogRpcVo;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.search.api.vo.RecommendationRpcVo;

public class SearchRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("script/*.painless");

        hints
            .reflection()
            .registerType(
                BlogDocument.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                AdminBlogDocument.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                AdminRevisionDocument.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        // Payload types bound by the functional routes and the index source client.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                BlogSnapshot.class,
                BlogIndexSourceStatus.class,
                Result.class,
                PageAdapter.class,
                BlogSysSearchReq.class,
                BlogSysCountSearchReq.class,
                BlogReadCountReq.class,
                BlogDocumentVo.class,
                IndexRebuildRpcVo.class,
                BlogAdminIndexMessage.class,
                BlogAdminSnapshot.class,
                BlogDraftSnapshot.class,
                BlogRevisionSnapshot.class,
                BlogSensitiveSpan.class,
                BlogRevisionSearchReq.class,
                AdminBlogRpcVo.class,
                AdminBlogSearchRpcVo.class,
                AdminRevisionRpcVo.class,
                AdminRevisionSearchRpcVo.class,
                PublicBlogRpcVo.class,
                RecommendationRpcVo.class);
    }
}
