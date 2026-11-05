package wiki.chiu.micro.search.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.web.service.registry.ImportHttpServices;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.search.MicroSearchApplication;
import wiki.chiu.micro.search.adapter.in.http.BlogDocumentVo;
import wiki.chiu.micro.search.adapter.in.http.IndexMaintenanceHttpHandler;
import wiki.chiu.micro.search.adapter.in.http.SearchInternalHttpHandler;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.AdminBlogRpcVo;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.search.api.vo.RecommendationFeedRpcVo;
import wiki.chiu.micro.search.api.vo.SearchIndexesRebuildRpcVo;

class SearchRuntimeHintsTest {

    @Test
    void httpServiceInterfacesCoverRemovedManualBindings() {
        RuntimeHints hints = new RuntimeHints();
        var registrar = new ReflectiveRuntimeHintsRegistrar();
        for (var group : MicroSearchApplication.class.getAnnotationsByType(ImportHttpServices.class)) {
            registrar.registerRuntimeHints(hints, group.types());
        }
        registrar.registerRuntimeHints(
            hints,
            SearchInternalHttpHandler.class,
            IndexMaintenanceHttpHandler.class);

        for (Class<?> type : List.of(
            BlogSnapshot.class,
            BlogIndexSourceStatus.class,
            Result.class,
            BlogSysSearchReq.class,
            BlogReadCountReq.class,
            IndexRebuildRpcVo.class,
            BlogAdminSnapshot.class,
            BlogAdminIndexSnapshot.class,
            BlogDraftSnapshot.class,
            BlogRevisionSnapshot.class,
            BlogSensitiveSpan.class,
            BlogRevisionSearchReq.class,
            AdminBlogRpcVo.class,
            AdminBlogSearchRpcVo.class,
            AdminRevisionRpcVo.class,
            AdminRevisionSearchRpcVo.class,
            PublicBlogRpcVo.class,
            RecommendationFeedRpcVo.class,
            SearchIndexesRebuildRpcVo.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsBindingsForFunctionalRoutesAndManualSerialization() {
        RuntimeHints hints = new RuntimeHints();
        new SearchRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(
            PageAdapter.class,
            BlogDocumentVo.class,
            BlogAdminIndexMessage.class,
            BlogAdminSnapshot.class,
            BlogDraftSnapshot.class,
            BlogRevisionSnapshot.class,
            BlogSensitiveSpan.class)) {
            assertBinding(hints, type);
        }
    }

    @Test
    void retainsScriptResources() {
        RuntimeHints hints = new RuntimeHints();
        new SearchRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource("script/blog-admin-update.painless").test(hints))
            .as("Resource hints for blog-admin-update.painless")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/blog-content-update.painless").test(hints))
            .as("Resource hints for blog-content-update.painless")
            .isTrue();
        assertThat(RuntimeHintsPredicates.resource().forResource("script/blog-read-count-update.painless").test(hints))
            .as("Resource hints for blog-read-count-update.painless")
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
