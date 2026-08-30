package wiki.chiu.micro.blog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.blog.adapter.in.http.BlogHttpHandler;
import wiki.chiu.micro.blog.adapter.in.http.BlogInternalHttpHandler;
import wiki.chiu.micro.blog.adapter.in.http.BlogRoutes;
import wiki.chiu.micro.blog.application.port.in.BlogAssetService;
import wiki.chiu.micro.blog.application.port.in.BlogCollaborationService;
import wiki.chiu.micro.blog.application.port.in.BlogExportService;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.auth.web.AuthPrincipalCodec;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.web.ValidatedRequest;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

  @Mock private BlogService blogService;

  @Mock private BlogAssetService assetService;

  @Mock private BlogCollaborationService collaborationService;

  @Mock private BlogExportService exportService;

  @Mock private BlogInternalHttpHandler blogInternalHttpHandler;

  private final ValidatedRequest validation = new ValidatedRequest();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    AuthPrincipal authInfo = new AuthPrincipal(1L, List.of("ROLE_USER"));
    BlogHttpHandler handler =
        new BlogHttpHandler(
            blogService, assetService, collaborationService, exportService, validation);
    mockMvc =
        MockMvcBuilders.routerFunctions(BlogRoutes.routes(handler, blogInternalHttpHandler))
            .defaultRequest(
                get("/")
                    .header(AuthPrincipalCodec.HEADER_NAME, AuthPrincipalCodec.encode(authInfo)))
            .build();
  }

  @Test
  void saveOrUpdateReturnsSuccess() throws Exception {
    doNothing().when(blogService).saveOrUpdate(any(), anyLong(), anyList());

    String body =
        "{\"id\":null,\"title\":\"t\",\"description\":\"d\",\"content\":\"c\","
            + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":[]}";
    mockMvc
        .perform(post("/sys/blog/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    verify(blogService).saveOrUpdate(any(), anyLong(), anyList());
  }

  @Test
  void concurrentUpdateReturnsConflict() throws Exception {
    doThrow(new BaseException(CommonErrorCode.CONFLICT, "blog revision conflict: 7"))
        .when(blogService)
        .saveOrUpdate(any(), anyLong(), anyList());

    String body =
        "{\"id\":7,\"title\":\"t\",\"description\":\"d\",\"content\":\"c\","
            + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":[]}";
    mockMvc
        .perform(post("/sys/blog/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(CommonErrorCode.CONFLICT.code()));
  }

  @Test
  void deleteBlogsReturnsSuccess() throws Exception {
    doNothing().when(blogService).deleteBatch(anyList(), anyLong(), anyList());

    mockMvc
        .perform(
            post("/sys/blog/delete").contentType(MediaType.APPLICATION_JSON).content("[1,2,3]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void setBlogTokenReturnsToken() throws Exception {
    when(collaborationService.issueReadToken(7L, 1L, List.of())).thenReturn("xyz-token");

    mockMvc
        .perform(post("/sys/blog/lock/7").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("xyz-token"));
  }

  @Test
  void getAllBlogsReturnsPage() throws Exception {
    BlogEntityVo vo = BlogEntityVo.builder().id(1L).title("t").build();
    PageAdapter<BlogEntityVo> page =
        PageAdapter.<BlogEntityVo>builder()
            .content(List.of(vo))
            .totalElements(1)
            .pageNumber(1)
            .pageSize(10)
            .first(true)
            .last(true)
            .empty(false)
            .totalPages(1)
            .build();
    when(blogService.findAllBlogs(any(), anyLong(), anyList())).thenReturn(page);

    mockMvc
        .perform(get("/sys/blog/blogs").param("currentPage", "1").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].id").value(1));
  }

  @Test
  void getDeletedBlogsReturnsPage() throws Exception {
    when(blogService.findDeletedBlogs(1, 10, 1L)).thenReturn(PageAdapter.emptyPage());

    mockMvc
        .perform(get("/sys/blog/deleted").param("currentPage", "1").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.empty").value(true));
  }

  @Test
  void recoverDeletedBlogReturnsSuccess() throws Exception {
    doNothing().when(blogService).recoverDeletedBlog(2, 1L);

    mockMvc
        .perform(post("/sys/blog/recover/2").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void uploadOssReturnsUrl() throws Exception {
    when(assetService.upload(any(), any(), anyLong(), anyList())).thenReturn("https://oss/x.png");
    MockMultipartFile file =
        new MockMultipartFile("image", "x.png", "image/png", new byte[] {1, 2, 3});

    mockMvc
        .perform(multipart("/sys/blog/oss/upload").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("https://oss/x.png"));
  }

  @Test
  void deleteOssReturnsSuccess() throws Exception {
    doNothing().when(assetService).delete("https://oss/x.png", null, 1L, List.of());

    mockMvc
        .perform(
            delete("/sys/blog/oss/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://oss/x.png\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void getEchoDetailWhenMissingReturns404() throws Exception {
    when(blogService.findEdit(any(), anyLong(), anyList()))
        .thenThrow(new MissException("not found"));

    mockMvc
        .perform(get("/sys/blog/edit/pull/echo").param("blogId", "9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value("not found"));
  }

  @Test
  void unknownPathReturns404() throws Exception {
    mockMvc.perform(get("/sys/blog/unknown")).andExpect(status().isNotFound());
  }

  @Test
  void deleteBlogsWithEmptyListIsRejectedBeforeHandler() throws Exception {
    mockMvc
        .perform(post("/sys/blog/delete").contentType(MediaType.APPLICATION_JSON).content("[]"))
        .andExpect(status().isBadRequest());

    verify(blogService, never()).deleteBatch(anyList(), anyLong(), anyList());
  }

  @Test
  void invalidBlogBodyIsRejectedBeforeHandler() throws Exception {
    mockMvc
        .perform(post("/sys/blog/save").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("title must not be blank"));

    verify(blogService, never()).saveOrUpdate(any(), anyLong(), anyList());
  }

  @Test
  void invalidSensitiveContentIsRejectedBeforeHandler() throws Exception {
    String body =
        "{\"id\":null,\"title\":\"t\",\"description\":\"d\",\"content\":\"c\","
            + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":["
            + "{\"startIndex\":0,\"endIndex\":1,\"type\":9}]}";

    mockMvc
        .perform(post("/sys/blog/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("sensitiveContent type is invalid"));

    verify(blogService, never()).saveOrUpdate(any(), anyLong(), anyList());
  }

  @Test
  void sensitiveContentOutsideContentIsRejectedBeforeHandler() throws Exception {
    String body =
        "{\"id\":null,\"title\":\"t\",\"description\":\"d\",\"content\":\"abc\","
            + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":["
            + "{\"startIndex\":1,\"endIndex\":4,\"type\":1}]}";

    mockMvc
        .perform(post("/sys/blog/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("sensitiveContent start/end indices invalid"));

    verify(blogService, never()).saveOrUpdate(any(), anyLong(), anyList());
  }

  @Test
  void deleteBlogsRejectsNonPositiveId() throws Exception {
    mockMvc
        .perform(post("/sys/blog/delete").contentType(MediaType.APPLICATION_JSON).content("[0]"))
        .andExpect(status().isBadRequest());

    verify(blogService, never()).deleteBatch(anyList(), anyLong(), anyList());
  }

  @Test
  void nonPositivePageIsRejectedBeforeHandler() throws Exception {
    mockMvc
        .perform(get("/sys/blog/blogs").param("currentPage", "0").param("size", "10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("currentPage must be positive"));

    verify(blogService, never()).findAllBlogs(any(), anyLong(), anyList());
  }

  @Test
  void invalidDateRangeIsRejectedBeforeHandler() throws Exception {
    mockMvc
        .perform(
            get("/sys/blog/blogs")
                .param("currentPage", "1")
                .param("size", "10")
                .param("createStart", "2026-08-02T12:00:00")
                .param("createEnd", "2026-08-02T11:00:00"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("createStart must not be after createEnd"));

    verify(blogService, never()).findAllBlogs(any(), anyLong(), anyList());
  }

  @Test
  void internalPageQueryUsesGetAndIsNotCapturedAsBlogId() throws Exception {
    when(blogInternalHttpHandler.findPage(any())).thenReturn(ServerResponse.ok().build());

    mockMvc
        .perform(get("/inner/blog/page").param("pageNo", "1").param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(blogInternalHttpHandler).findPage(any());
  }

  @Test
  void internalViewMutationUsesExplicitViewsPath() throws Exception {
    when(blogInternalHttpHandler.setReadCount(any(ServerRequest.class)))
        .thenReturn(ServerResponse.ok().build());

    mockMvc.perform(post("/inner/blog/7/views")).andExpect(status().isOk());

    verify(blogInternalHttpHandler).setReadCount(any(ServerRequest.class));
  }
}
