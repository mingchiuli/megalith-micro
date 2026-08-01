package wiki.chiu.micro.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.blog.handler.BlogHttpHandler;
import wiki.chiu.micro.blog.handler.BlogInternalHttpHandler;
import wiki.chiu.micro.blog.route.BlogRoutes;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    @Mock
    private BlogService blogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthInfo authInfo = new AuthInfo(1L, List.of("ROLE_USER"), List.of());
        AuthHttpService authHttpService = org.mockito.Mockito.mock(AuthHttpService.class);
        org.mockito.Mockito.lenient().when(authHttpService.getAuthentication(anyString())).thenReturn(Result.success(
                new AuthRpcVo(authInfo.userId(), authInfo.roles(), authInfo.authorities())));
        ValidatedRequest validation = new ValidatedRequest(jakarta.validation.Validation
                .buildDefaultValidatorFactory().getValidator());
        BlogHttpHandler handler = new BlogHttpHandler(blogService, authHttpService, validation);
        mockMvc = MockMvcBuilders.routerFunctions(BlogRoutes.routes(
                        handler, org.mockito.Mockito.mock(BlogInternalHttpHandler.class)))
                .build();
    }

    @Test
    void saveOrUpdateReturnsSuccess() throws Exception {
        doNothing().when(blogService).saveOrUpdate(any(), anyLong(), anyList());

        String body = "{\"id\":null,\"title\":\"t\",\"description\":\"d\",\"content\":\"c\","
                + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":[]}";
        mockMvc.perform(post("/sys/blog/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(blogService).saveOrUpdate(any(), anyLong(), anyList());
    }

    @Test
    void deleteBlogsReturnsSuccess() throws Exception {
        doNothing().when(blogService).deleteBatch(anyList(), anyLong(), anyList());

        mockMvc.perform(post("/sys/blog/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void setBlogTokenReturnsToken() throws Exception {
        when(blogService.setBlogToken(7L, 1L)).thenReturn("xyz-token");

        mockMvc.perform(get("/sys/blog/lock/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("xyz-token"));
    }

    @Test
    void getAllBlogsReturnsPage() throws Exception {
        BlogEntityVo vo = BlogEntityVo.builder().id(1L).title("t").build();
        PageAdapter<BlogEntityVo> page = PageAdapter.<BlogEntityVo>builder()
                .content(List.of(vo)).totalElements(1).pageNumber(1).pageSize(10)
                .first(true).last(true).empty(false).totalPages(1).build();
        when(blogService.findAllBlogs(any(), anyLong(), anyList())).thenReturn(page);

        mockMvc.perform(get("/sys/blog/blogs")
                        .param("currentPage", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getDeletedBlogsReturnsPage() throws Exception {
        when(blogService.findDeletedBlogs(1, 10, 1L)).thenReturn(PageAdapter.emptyPage());

        mockMvc.perform(get("/sys/blog/deleted")
                        .param("currentPage", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    void recoverDeletedBlogReturnsSuccess() throws Exception {
        doNothing().when(blogService).recoverDeletedBlog(2, 1L);

        mockMvc.perform(get("/sys/blog/recover/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void uploadOssReturnsUrl() throws Exception {
        when(blogService.uploadOss(any(), anyLong())).thenReturn("https://oss/x.png");
        MockMultipartFile file = new MockMultipartFile("image", "x.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/sys/blog/oss/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://oss/x.png"));
    }

    @Test
    void deleteOssReturnsSuccess() throws Exception {
        doNothing().when(blogService).deleteOss("https://oss/x.png");

        mockMvc.perform(get("/sys/blog/oss/delete").param("url", "https://oss/x.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getEchoDetailWhenServiceThrowsReturns400() throws Exception {
        when(blogService.findEdit(any(), anyLong(), anyList()))
                .thenThrow(new MissException("not found"));

        mockMvc.perform(get("/sys/blog/edit/pull/echo").param("blogId", "9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/blog/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBlogsWithEmptyListIsRejectedBeforeHandler() throws Exception {
        mockMvc.perform(post("/sys/blog/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        verify(blogService, never()).deleteBatch(anyList(), anyLong(), anyList());
    }

    @Test
    void invalidBlogBodyIsRejectedBeforeHandler() throws Exception {
        mockMvc.perform(post("/sys/blog/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("param error"));

        verify(blogService, never()).saveOrUpdate(any(), anyLong(), anyList());
    }

    @Test
    void invalidSensitiveContentIsRejectedBeforeHandler() throws Exception {
        String body = "{\"id\":null,\"title\":\"t\",\"description\":\"d\",\"content\":\"c\","
                + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":["
                + "{\"startIndex\":0,\"endIndex\":1,\"type\":9}]}";

        mockMvc.perform(post("/sys/blog/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("param error"));

        verify(blogService, never()).saveOrUpdate(any(), anyLong(), anyList());
    }

    @Test
    void invalidDateRangeIsRejectedBeforeHandler() throws Exception {
        mockMvc.perform(get("/sys/blog/blogs")
                        .param("currentPage", "1")
                        .param("size", "10")
                        .param("createStart", "2026-08-02T12:00:00")
                        .param("createEnd", "2026-08-02T11:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("param error"));

        verify(blogService, never()).findAllBlogs(any(), anyLong(), anyList());
    }
}
