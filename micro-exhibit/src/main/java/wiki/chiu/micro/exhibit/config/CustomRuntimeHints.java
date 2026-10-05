package wiki.chiu.micro.exhibit.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.api.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogExhibitVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogHotReadVo;
import wiki.chiu.micro.exhibit.adapter.in.http.ReadTokenReq;
import wiki.chiu.micro.exhibit.adapter.in.http.VisitStatisticsVo;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.domain.SensitiveSpan;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types bound by the functional routes in adapter.in.http.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                Result.class,
                PageAdapter.class,
                ReadTokenReq.class,
                BlogDescriptionVo.class,
                BlogExhibitVo.class,
                BlogHotReadVo.class,
                VisitStatisticsVo.class);

        hints
            .reflection()
            .registerType(
                BlogExhibit.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogDescription.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                SensitiveSpan.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                PageAdapter.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogSensitiveContentRpcVo.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                SensitiveContentRpcVo.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);

        hints
            .resources()
            .registerPattern("script/multi-pfcount.lua")
            .registerPattern("script/compare-delete.lua");
    }
}
