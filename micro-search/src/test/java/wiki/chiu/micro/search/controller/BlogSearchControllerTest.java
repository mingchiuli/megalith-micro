package wiki.chiu.micro.search.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.exception.GlobalExceptionHandler;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogDocumentVo;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BlogSearchControllerTest {

    @Mock
    private BlogSearchService blogSearchService;

    @InjectMocks
    private BlogSearchController blogSearchController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(blogSearchController)
                .setControllerAdvice(new GlobalExceptionHandler())
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
    void searchBlogsServiceFailureReturns400() throws Exception {
        when(blogSearchService.selectBlogsByES(anyInt(), anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("es down"));

        mockMvc.perform(get("/search/public/blog")
                        .param("currentPage", "1")
                        .param("allInfo", "true")
                        .param("keywords", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("es down"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/search/unknown"))
                .andExpect(status().isNotFound());
    }
}
