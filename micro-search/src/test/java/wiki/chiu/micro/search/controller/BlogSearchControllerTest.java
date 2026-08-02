package wiki.chiu.micro.search.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.handler.BlogSearchHttpHandler;
import wiki.chiu.micro.search.handler.SearchInternalHttpHandler;
import wiki.chiu.micro.search.route.SearchRoutes;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BlogSearchControllerTest {

    @Mock
    private BlogSearchService blogSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ValidatedRequest validation = new ValidatedRequest(jakarta.validation.Validation
                .buildDefaultValidatorFactory().getValidator());
        BlogSearchHttpHandler handler = new BlogSearchHttpHandler(blogSearchService, validation);
        SearchInternalHttpHandler internalHandler = new SearchInternalHttpHandler(blogSearchService, validation);
        mockMvc = MockMvcBuilders.routerFunctions(
                        SearchRoutes.routes(handler, internalHandler))
                .build();
    }

    @Test
    void searchBlogsReturnsPage() throws Exception {
        BlogDocumentVo vo = BlogDocumentVo.builder()
                .id(1L).userId(2L).status(0).title("title").description("d").content("c")
                .link("").created(LocalDateTime.now()).score(1.0f).highlight(null).build();
        PageAdapter<BlogDocumentVo> page = PageAdapter.<BlogDocumentVo>builder()
                .content(List.of(vo)).totalElements(1).pageNumber(1).pageSize(10)
                .first(true).last(true).empty(false).totalPages(1).build();
        when(blogSearchService.selectBlogsByES(anyInt(), anyString(), anyBoolean())).thenReturn(page);

        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "1")
                        .param("allInfo", "true")
                        .param("keywords", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void searchBlogsMissingRequiredParamReturns400() throws Exception {
        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "1")
                        .param("allInfo", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchBlogsRejectsKeywordsLongerThanTwentyCharacters() throws Exception {
        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "1")
                        .param("allInfo", "true")
                        .param("keywords", "123456789012345678901"))
                .andExpect(status().isBadRequest());

        verify(blogSearchService, never()).selectBlogsByES(anyInt(), anyString(), anyBoolean());
    }

    @Test
    void searchBlogsRejectsNonPositivePage() throws Exception {
        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "0")
                        .param("allInfo", "true")
                        .param("keywords", "abc"))
                .andExpect(status().isBadRequest());

        verify(blogSearchService, never()).selectBlogsByES(anyInt(), anyString(), anyBoolean());
    }

    @Test
    void searchBlogsRejectsInvalidBoolean() throws Exception {
        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "1")
                        .param("allInfo", "anything")
                        .param("keywords", "abc"))
                .andExpect(status().isBadRequest());

        verify(blogSearchService, never()).selectBlogsByES(anyInt(), anyString(), anyBoolean());
    }

    @Test
    void internalSearchRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/inner/blog/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(blogSearchService, never()).searchBlogs(any());
    }

    @Test
    void internalCountRejectsInvalidDateRange() throws Exception {
        String body = "{\"status\":0,\"createStart\":\"2026-08-02T12:00:00\","
                + "\"createEnd\":\"2026-08-02T11:00:00\",\"userId\":1,\"roles\":[]}";

        mockMvc.perform(post("/inner/blog/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(blogSearchService, never()).searchCount(any());
    }

    @Test
    void searchBlogsServiceFailureReturns500() throws Exception {
        when(blogSearchService.selectBlogsByES(anyInt(), anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("es down"));

        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "1")
                        .param("allInfo", "true")
                        .param("keywords", "abc"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.msg").value("es down"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/search/unknown"))
                .andExpect(status().isNotFound());
    }
}
