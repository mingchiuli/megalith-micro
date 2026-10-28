package wiki.chiu.micro.exhibit.adapter.in.http;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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

import wiki.chiu.micro.common.auth.web.AuthPrincipalCodec;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.Page;
import wiki.chiu.micro.exhibit.application.model.VisitStatistics;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    @Mock
    private BlogService blogService;
    private final ValidatedRequest validation = new ValidatedRequest();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthPrincipal authInfo = new AuthPrincipal(1L, List.of("ROLE_USER"));
        BlogExhibitHttpHandler handler =
            new BlogExhibitHttpHandler(blogService, validation);
        mockMvc =
            MockMvcBuilders.routerFunctions(ExhibitRoutes.routes(handler))
                .defaultRequest(
                    get("/")
                        .header(AuthPrincipalCodec.HEADER_NAME, AuthPrincipalCodec.encode(authInfo)))
                .build();
    }

    private BlogExhibit sampleBlog() {
        return new BlogExhibit(1L, "desc", "n", "a", "t", "c", LocalDateTime.now(), 1L, 1);
    }

    @Test
    void getBlogDetailReturnsVo() throws Exception {
        when(blogService.getBlogDetail(anyList(), anyLong(), anyLong())).thenReturn(sampleBlog());

        mockMvc
            .perform(get("/public/blog/info/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.title").value("t"));
    }

    @Test
    void getBlogDetailWhenMissingReturns404() throws Exception {
        when(blogService.getBlogDetail(anyList(), anyLong(), anyLong()))
            .thenThrow(new MissException("not found"));

        mockMvc
            .perform(get("/public/blog/info/5"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void getLockedBlogReturnsVo() throws Exception {
        when(blogService.getLockedBlog(7L, "tk")).thenReturn(sampleBlog());

        mockMvc
            .perform(
                post("/public/blog/secret/7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"readToken\":\"tk\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("t"));
    }

    @Test
    void getVisitStatisticsReturnsVo() throws Exception {
        when(blogService.getVisitStatistics()).thenReturn(new VisitStatistics(1L, 2L, 3L, 4L));

        mockMvc
            .perform(get("/public/blog/stat"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.dayVisit").value(1));
    }

    @Test
    void getScoreBlogsReturnsList() throws Exception {
        when(blogService.getScoreBlogs()).thenReturn(List.of(new BlogHotRead(1L, "t", 10L)));

        mockMvc
            .perform(get("/public/blog/scores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getRecommendationPageReturnsPagedCards() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 10, 25, 8, 30, 15);
        Page<BlogRecommendation> page =
            new Page<>(
                List.of(new BlogRecommendation(7L, "title", "description", created,
                    "https://cdn.example/7.png", 10L)),
                6,
                2,
                5,
                false,
                true,
                false,
                2);
        when(blogService.findRecommendationPage(2)).thenReturn(page);

        mockMvc
            .perform(get("/public/blog/recommendations/page/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].id").value(7))
            .andExpect(jsonPath("$.data.content[0].created").value("2026-10-25 08:30:15"))
            .andExpect(jsonPath("$.data.content[0].link").value("https://cdn.example/7.png"))
            .andExpect(jsonPath("$.data.pageNumber").value(2))
            .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/public/blog/unknown")).andExpect(status().isNotFound());
    }
}
