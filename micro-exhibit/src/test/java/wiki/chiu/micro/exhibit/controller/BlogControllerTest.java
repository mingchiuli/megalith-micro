package wiki.chiu.micro.exhibit.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.exhibit.handler.BlogExhibitHttpHandler;
import wiki.chiu.micro.exhibit.route.ExhibitRoutes;
import wiki.chiu.micro.exhibit.service.BlogService;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

  @Mock private BlogService blogService;

  @Mock private AuthHttpService authHttpService;

  private final ValidatedRequest validation = new ValidatedRequest();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    AuthPrincipal authInfo = new AuthPrincipal(1L, List.of("ROLE_USER"), List.of());
    lenient()
        .when(authHttpService.getAuthentication(anyString()))
        .thenReturn(
            Result.success(
                new AuthRpcVo(authInfo.userId(), authInfo.roles(), authInfo.authorities())));
    BlogExhibitHttpHandler handler =
        new BlogExhibitHttpHandler(blogService, authHttpService, validation);
    mockMvc = MockMvcBuilders.routerFunctions(ExhibitRoutes.routes(handler)).build();
  }

  private BlogExhibitVo sampleBlog() {
    return BlogExhibitVo.builder()
        .description("desc")
        .nickname("n")
        .avatar("a")
        .title("t")
        .content("c")
        .created(LocalDateTime.now())
        .readCount(1L)
        .build();
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
  void getPageReturnsPage() throws Exception {
    when(blogService.findPage(1)).thenReturn(PageAdapter.emptyPage());

    mockMvc
        .perform(get("/public/blog/page/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.empty").value(true));
  }

  @Test
  void getPageWithBadPathVariableReturns400() throws Exception {
    mockMvc.perform(get("/public/blog/page/abc")).andExpect(status().isBadRequest());
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
    when(blogService.getVisitStatistics())
        .thenReturn(
            VisitStatisticsVo.builder()
                .dayVisit(1L)
                .weekVisit(2L)
                .monthVisit(3L)
                .yearVisit(4L)
                .build());

    mockMvc
        .perform(get("/public/blog/stat"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.dayVisit").value(1));
  }

  @Test
  void getScoreBlogsReturnsList() throws Exception {
    when(blogService.getScoreBlogs())
        .thenReturn(List.of(BlogHotReadVo.builder().id(1L).title("t").readCount(10L).build()));

    mockMvc
        .perform(get("/public/blog/scores"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(1));
  }

  @Test
  void unknownPathReturns404() throws Exception {
    mockMvc.perform(get("/public/blog/unknown")).andExpect(status().isNotFound());
  }
}
