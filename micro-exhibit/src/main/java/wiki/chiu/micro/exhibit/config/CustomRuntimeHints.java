package wiki.chiu.micro.exhibit.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.api.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;
import wiki.chiu.micro.exhibit.req.ReadTokenReq;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

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
                BlogExhibitDto.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(
                BlogDescriptionDto.class,
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
