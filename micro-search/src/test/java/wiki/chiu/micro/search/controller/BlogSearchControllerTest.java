package wiki.chiu.micro.search.controller;

import static org.mockito.ArgumentMatchers.any;
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
import wiki.chiu.micro.search.adapter.in.http.BlogDocumentVo;
import wiki.chiu.micro.search.adapter.in.http.BlogSearchHttpHandler;
import wiki.chiu.micro.search.adapter.in.http.SearchInternalHttpHandler;
import wiki.chiu.micro.search.adapter.in.http.SearchRoutes;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.SearchPage;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;

@ExtendWith(MockitoExtension.class)
class BlogSearchControllerTest {

  @Mock private SearchBlogsUseCase searchBlogs;

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
            .link("")
            .created(LocalDateTime.now())
            .score(1.0f)
            .highlight(null)
            .build();
    SearchPage<BlogSearchHit> page =
        new SearchPage<>(
            List.of(
                new BlogSearchHit(
                    vo.id(),
                    vo.userId(),
                    vo.status(),
                    vo.title(),
                    vo.description(),
                    vo.content(),
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
        .andExpect(jsonPath("$.data.content[0].id").value(1));
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
  void internalSearchRejectsInvalidBody() throws Exception {
    mockMvc
        .perform(post("/inner/blog/search").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());

    verify(searchBlogs, never()).searchPrivate(any());
  }

  @Test
  void internalCountRejectsInvalidDateRange() throws Exception {
    String body =
        "{\"status\":0,\"createStart\":\"2026-08-02T12:00:00\","
            + "\"createEnd\":\"2026-08-02T11:00:00\",\"userId\":1,\"allData\":false}";

    mockMvc
        .perform(post("/inner/blog/count").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());

    verify(searchBlogs, never()).countPrivate(any());
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
  void unknownPathReturns404() throws Exception {
    mockMvc.perform(get("/search/unknown")).andExpect(status().isNotFound());
  }

  @Test
  void internalViewMutationUsesExplicitViewsPath() throws Exception {
    mockMvc.perform(post("/inner/blog/7/views")).andExpect(status().isOk());

    verify(searchBlogs).incrementViews(7L);
  }
}
