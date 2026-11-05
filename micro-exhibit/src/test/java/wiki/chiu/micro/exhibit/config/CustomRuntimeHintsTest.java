package wiki.chiu.micro.exhibit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.exhibit.MicroExhibitApplication;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogExhibitVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogHotReadVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogRecommendationFeedVo;
import wiki.chiu.micro.exhibit.adapter.in.http.BlogRecommendationVo;
import wiki.chiu.micro.exhibit.adapter.in.http.ReadTokenReq;
import wiki.chiu.micro.exhibit.adapter.in.http.VisitStatisticsVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;

class CustomRuntimeHintsTest {

    @Test
    void httpServiceInterfacesCoverRemovedManualBindings() {
        RuntimeHints hints = new RuntimeHints();
        var registrar = new ReflectiveRuntimeHintsRegistrar();
        for (var group : MicroExhibitApplication.class.getAnnotationsByType(ImportHttpServices.class)) {
            registrar.registerRuntimeHints(hints, group.types());
        }

        for (Class<?> type : List.of(
            Result.class,
            PublicBlogRpcVo.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsBindingsForFunctionalRoutesAndManualSerialization() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(
            ReadTokenReq.class,
            BlogExhibitVo.class,
            BlogHotReadVo.class,
            BlogRecommendationVo.class,
            BlogRecommendationFeedVo.class,
            VisitStatisticsVo.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsScriptResources() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource("script/compare-delete.lua").test(hints))
            .as("Resource hints for compare-delete.lua")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/multi-pfcount.lua").test(hints))
            .as("Resource hints for multi-pfcount.lua")
            .isTrue();
    }

    private void assertBinding(RuntimeHints hints, Class<?> type) {
        if (type.isEnum()) {
            assertThat(RuntimeHintsPredicates.reflection()
                .onType(type)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS)
                .or(RuntimeHintsPredicates.reflection().onMethodInvocation(type, "values"))
                .test(hints))
                .as("Enum binding hints for %s", type.getName())
                .isTrue();
            return;
        }
        assertThat(RuntimeHintsPredicates.reflection()
            .onType(type)
            .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .test(hints))
            .as("Binding constructor hints for %s", type.getName())
            .isTrue();
        for (var component : type.getRecordComponents()) {
            assertThat(RuntimeHintsPredicates.reflection()
                .onType(type)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS)
                .or(RuntimeHintsPredicates.reflection().onMethodInvocation(component.getAccessor()))
                .test(hints))
                .as("Binding accessor hints for %s.%s", type.getName(), component.getName())
                .isTrue();
        }
    }
}
