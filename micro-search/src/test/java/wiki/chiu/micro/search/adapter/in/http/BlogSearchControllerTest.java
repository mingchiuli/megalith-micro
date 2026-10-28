package wiki.chiu.micro.search.adapter.in.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;

@ExtendWith(MockitoExtension.class)
class BlogSearchControllerTest {

    @Mock
    private SearchBlogsUseCase searchBlogs;

    private final ValidatedRequest validation = new ValidatedRequest();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BlogSearchHttpHandler handler = new BlogSearchHttpHandler(searchBlogs, validation);
        SearchInternalHttpHandler internalHandler =
            new SearchInternalHttpHandler(searchBlogs, validation);
        mockMvc =
            MockMvcBuilders.routerFunctions(SearchRoutes.routes(handler, internalHandler)).build();
    }

    @Test
    void searchBlogsReturnsPage() throws Exception {
        BlogDocumentVo vo =
            BlogDocumentVo.builder()
                .id(1L)
                .userId(2L)
                .status(0)
                .title("title")
                .description("d")
                .content("c")
                .link("https://cdn.example/cover-1.png")
                .created(LocalDateTime.now())
                .score(1.0f)
                .highlight(null)
                .build();
        Page<BlogSearchHit> page =
            new Page<>(
                List.of(
                    new BlogSearchHit(
                        vo.id(),
                        vo.userId(),
                        vo.status(),
                        vo.title(),
                        vo.description(),
                        vo.content(),
                        vo.link(),
                        vo.created(),
                        vo.score(),
                        vo.highlight())),
                1,
                1,
                10,
                true,
                true,
                false,
                1);
        when(searchBlogs.searchPublic(any(PublicBlogSearchQuery.class))).thenReturn(page);

        mockMvc
            .perform(
                get("/search/public/blog")
                    .param("currentPage", "1")
                    .param("allInfo", "true")
                    .param("keywords", "abc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content[0].id").value(1))
            .andExpect(jsonPath("$.data.content[0].link").value("https://cdn.example/cover-1.png"));
    }

    @Test
    void searchBlogsMissingRequiredParamReturns400() throws Exception {
        mockMvc
            .perform(get("/search/public/blog").param("currentPage", "1").param("allInfo", "true"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void searchBlogsRejectsKeywordsLongerThanTwentyCharacters() throws Exception {
        mockMvc
            .perform(
                get("/search/public/blog")
                    .param("currentPage", "1")
                    .param("allInfo", "true")
                    .param("keywords", "123456789012345678901"))
            .andExpect(status().isBadRequest());

        verify(searchBlogs, never()).searchPublic(any());
    }

    @Test
    void searchBlogsRejectsNonPositivePage() throws Exception {
        mockMvc
            .perform(
                get("/search/public/blog")
                    .param("currentPage", "0")
                    .param("allInfo", "true")
                    .param("keywords", "abc"))
            .andExpect(status().isBadRequest());

        verify(searchBlogs, never()).searchPublic(any());
    }

    @Test
    void searchBlogsRejectsInvalidBoolean() throws Exception {
        mockMvc
            .perform(
                get("/search/public/blog")
                    .param("currentPage", "1")
                    .param("allInfo", "anything")
                    .param("keywords", "abc"))
            .andExpect(status().isBadRequest());

        verify(searchBlogs, never()).searchPublic(any());
    }

    @Test
    void searchBlogsServiceFailureReturns500() throws Exception {
        when(searchBlogs.searchPublic(any()))
            .thenThrow(new RuntimeException("es down"));

        mockMvc
            .perform(
                get("/search/public/blog")
                    .param("currentPage", "1")
                    .param("allInfo", "true")
                    .param("keywords", "abc"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.msg").value("es down"));
    }

    @Test
    void recommendationPageReturnsPagingMetadata() throws Exception {
        Page<PublicBlogRead> page =
            new Page<>(
                List.of(publicBlog(7)),
                6,
                2,
                5,
                false,
                true,
                false,
                2);
        when(searchBlogs.recommendationPage(2, 5)).thenReturn(page);

        mockMvc
            .perform(
                get("/inner/blog/public/recommendations/page")
                    .param("page", "2")
                    .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].id").value(7))
            .andExpect(jsonPath("$.data.pageNumber").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(6))
            .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void recommendationPageRejectsInvalidPaging() throws Exception {
        mockMvc
            .perform(
                get("/inner/blog/public/recommendations/page")
                    .param("page", "0")
                    .param("size", "5"))
            .andExpect(status().isBadRequest());

        verify(searchBlogs, never()).recommendationPage(anyInt(), anyInt());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/search/unknown")).andExpect(status().isNotFound());
    }

    @Test
    void internalViewMutationUsesExplicitViewsPath() throws Exception {
        mockMvc.perform(post("/inner/blog/views/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[{\"blogId\":7,\"readCount\":120}]"))
            .andExpect(status().isOk());

        verify(searchBlogs).updateReadCounts(List.of(new BlogReadCount(7L, 120L)));
    }

    @Test
    void batchStatisticsRejectInvalidCountsAndEmptyBatches() throws Exception {
        for (String body : List.of("[]", "[null]", "[{\"blogId\":0,\"readCount\":1}]", "[{\"blogId\":7,\"readCount\":-1}]")) {
            mockMvc.perform(post("/inner/blog/views/batch").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
        verify(searchBlogs, never()).updateReadCounts(any());
    }

    private static PublicBlogRead publicBlog(long id) {
        LocalDateTime created = LocalDateTime.of(2026, 10, 25, 12, 0);
        return new PublicBlogRead(
            id,
            2L,
            null,
            null,
            "title",
            "description",
            "content",
            "",
            10L,
            created,
            created,
            null,
            null,
            null,
            List.of());
    }
}
