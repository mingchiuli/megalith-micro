package wiki.chiu.micro.blog.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.blog.MicroBlogApplication;
import wiki.chiu.micro.blog.adapter.in.http.BlogDeleteVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogDownloadReq;
import wiki.chiu.micro.blog.adapter.in.http.BlogEditVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogEntityReq;
import wiki.chiu.micro.blog.adapter.in.http.BlogEntityVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogIdVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogIndexSourceHttpHandler;
import wiki.chiu.micro.blog.adapter.in.http.BlogInternalHttpHandler;
import wiki.chiu.micro.blog.adapter.in.http.BlogPermissionsVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogQueryReq;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionComparisonVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionSaveVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionSummaryVo;
import wiki.chiu.micro.blog.adapter.in.http.BlogVersionVo;
import wiki.chiu.micro.blog.adapter.in.http.OssDeleteReq;
import wiki.chiu.micro.blog.adapter.in.http.PublishBlogReq;
import wiki.chiu.micro.blog.adapter.in.http.SensitiveContentReq;
import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;

class CustomRuntimeHintsTest {

    @Test
    void httpServiceInterfacesCoverRemovedManualBindings() {
        RuntimeHints hints = new RuntimeHints();
        var registrar = new ReflectiveRuntimeHintsRegistrar();
        for (var group : MicroBlogApplication.class.getAnnotationsByType(ImportHttpServices.class)) {
            registrar.registerRuntimeHints(hints, group.types());
        }
        registrar.registerRuntimeHints(
            hints,
            BlogInternalHttpHandler.class,
            BlogIndexSourceHttpHandler.class);

        for (Class<?> type : List.of(
            Result.class,
            BlogIndexSourceStatus.class,
            BlogAdminIndexSnapshot.class,
            BlogSnapshot.class,
            BlogAdminSnapshot.class,
            BlogDraftSnapshot.class,
            BlogRevisionSnapshot.class,
            BlogSensitiveSpan.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsBindingsForFunctionalRoutesAndManualSerialization() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(
            PageAdapter.class,
            BlogEntityReq.class,
            BlogIdVo.class,
            SensitiveContentReq.class,
            BlogQueryReq.class,
            PublishBlogReq.class,
            BlogDownloadReq.class,
            OssDeleteReq.class,
            BlogEntityVo.class,
            BlogDeleteVo.class,
            BlogEditVo.class,
            BlogVersionVo.class,
            BlogVersionSaveVo.class,
            BlogVersionSummaryVo.class,
            BlogVersionComparisonVo.class,
            BlogPermissionsVo.class,
            BlogWorkingDraft.class,
            Blog.class,
            BlogChangedMessage.class,
            BlogAdminIndexMessage.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsScriptResources() {
        RuntimeHints hints = new RuntimeHints();
        new CustomRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource("script/blog-delete-list.lua").test(hints))
            .as("Resource hints for blog-delete-list.lua")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/blog-recycle.lua").test(hints))
            .as("Resource hints for blog-recycle.lua")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/hot-blogs.lua").test(hints))
            .as("Resource hints for hot-blogs.lua")
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
