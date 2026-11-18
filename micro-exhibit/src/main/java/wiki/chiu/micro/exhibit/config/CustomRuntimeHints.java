package wiki.chiu.micro.exhibit.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.api.vo.BlogPublicationContentRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogPublicationRpcVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogExhibitVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogRecommendationFeedVo;
import wiki.chiu.micro.exhibit.adapter.in.http.ReadTokenReq;
import wiki.chiu.micro.exhibit.adapter.in.http.VisitStatisticsVo;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // HTTP payload types not covered by HTTP service interfaces.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                BlogPublicationRpcVo.class,
                BlogPublicationContentRpcVo.class,
                ReadTokenReq.class,
                BlogExhibitVo.class,
                BlogRecommendationFeedVo.class,
                VisitStatisticsVo.class);

        hints
            .resources()
            .registerPattern("script/multi-pfcount.lua")
            .registerPattern("script/compare-delete.lua");
    }
}
